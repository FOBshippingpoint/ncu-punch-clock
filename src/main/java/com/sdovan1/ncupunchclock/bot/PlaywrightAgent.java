package com.sdovan1.ncupunchclock.bot;

import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.driver.jar.DriverJar;
import com.microsoft.playwright.options.AriaRole;
import com.sdovan1.ncupunchclock.schedule.ClockInFailedException;
import com.sdovan1.ncupunchclock.schedule.ClockOutFailedException;
import com.sdovan1.ncupunchclock.schedule.LoginFailedException;
import com.sdovan1.ncupunchclock.schedule.Punch;
import org.springframework.security.core.parameters.P;

public class PlaywrightAgent implements PunchAgent {
    private static final String LOGIN_URL = "https://portal.ncu.edu.tw/login";
    private final String punchUrl;
    private final String username;
    private final String password;
    private final String jobDescription;
    private final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();

    public static final String MSG_USERNAME_OR_PASSWORD_WRONG = "您輸入的密碼不正確";
    public static final String MSG_PLEASE_CHECK_CAPTCHA = "請勾我不是機器人";

    public PlaywrightAgent(String partTimeUsuallyId, String username, String password, String jobDescription) {
        this.punchUrl = Punch.partTimeUsuallyIdToUrl(partTimeUsuallyId);
        this.username = username;
        this.password = password;
        this.jobDescription = jobDescription;
    }

    public PlaywrightAgent(String punchUrl, String username, String password, String jobDescription, boolean headless) {
        this.punchUrl = punchUrl;
        this.username = username;
        this.password = password;
        this.jobDescription = jobDescription;
        this.launchOptions.setHeadless(headless);
    }

    public void loadBrowserAndDo(BrowserDo browserDo) {
        var originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DriverJar.class.getClassLoader());
        try (var playwright = Playwright.create()) {
            try (var browser = playwright.chromium().launch(launchOptions)) {
                browserDo.doWithBrowser(browser);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public void clockIn() throws LoginFailedException, ClockInFailedException {
        loadBrowserAndDo((browser) -> {
            var page = login(browser);
            try {
                page.navigate(punchUrl);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("確定前往")).click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("簽到")).click();
                if (!isMessageEqual(page, "簽到完成")) {
                    throw new ClockInFailedException("簽到失敗");
                }
            } catch (PlaywrightException e) {
                throw new ClockInFailedException("簽到失敗", e);
            }
        });
    }

    public void clockOut() throws LoginFailedException, ClockOutFailedException {
        loadBrowserAndDo(browser -> {
            var page = login(browser);
            try {
                page.navigate(punchUrl);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("確定前往")).click();
                page.locator("#AttendWork").fill(jobDescription);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("簽退")).click();
                if (!isMessageEqual(page, "簽退完成")) {
                    throw new ClockOutFailedException("簽退失敗");
                }
            } catch (PlaywrightException e) {
                throw new ClockOutFailedException("簽退失敗", e);
            }
        });
    }

    private Page login(Browser browser) throws LoginFailedException {
        try {
            var context = browser.newContext();
            var page = context.newPage();
            page.navigate(LOGIN_URL);
            var isSwitchToChineseBtnExists = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("中文版")).isVisible();
            if (isSwitchToChineseBtnExists) {
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("中文版")).click();
            }
            page.getByLabel("帳號").fill(username);
            page.getByLabel("密碼").fill(password);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登入 Portal")).click();
            if (isErrorMessagesEqual(page, MSG_USERNAME_OR_PASSWORD_WRONG)) {
                throw new LoginFailedException(LoginFailedException.WRONG_USERNAME_OR_PASSWORD);
            }
            if (isErrorMessagesEqual(page, MSG_PLEASE_CHECK_CAPTCHA)) {
                throw new LoginFailedException(LoginFailedException.FAILED_TO_PASS_CAPTCHA);
            }
            return page;
        } catch (PlaywrightException e) {
            throw new LoginFailedException("登入失敗", e);
        }
    }

    public void loginCheck() throws LoginFailedException {
        loadBrowserAndDo(this::login);
    }

    private static boolean isErrorMessagesEqual(Page page, String msg) {
        if (page.getByRole(AriaRole.PARAGRAPH).isVisible()) {
            return page.getByRole(AriaRole.PARAGRAPH).textContent().equals(msg);
        } else {
            return false;
        }
    }

    private static boolean isMessageEqual(Page page, String msg) {
        return page.locator("#msg").textContent().equals(msg);
    }

    @FunctionalInterface
    public interface BrowserDo {
        void doWithBrowser(Browser browser);
    }
}
