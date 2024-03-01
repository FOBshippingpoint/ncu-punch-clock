package com.sdovan1.ncupunchclock.bot;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.sdovan1.ncupunchclock.schedule.ClockInFailedException;
import com.sdovan1.ncupunchclock.schedule.ClockOutFailedException;
import com.sdovan1.ncupunchclock.schedule.LoginFailedException;
import com.sdovan1.ncupunchclock.schedule.Punch;

public class PlaywrightAgent implements PunchAgent {
    private static final String LOGIN_URL = "https://portal.ncu.edu.tw/login";
    private final String punchUrl;
    private final String username;
    private final String password;
    private final String jobDescription;

    public static final String MSG_USERNAME_OR_PASSWORD_WRONG = "您輸入的密碼不正確";
    public static final String MSG_PLEASE_CHECK_CAPTCHA = "請勾我不是機器人";

    public PlaywrightAgent(String partTimeUsuallyId, String username, String password, String jobDescription) {
        this.punchUrl = Punch.partTimeUsuallyIdToUrl(partTimeUsuallyId);
        this.username = username;
        this.password = password;
        this.jobDescription = jobDescription;
    }

    public void clockIn() throws LoginFailedException, ClockInFailedException {
        try (var playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
            try (var browser = playwright.chromium().launch(launchOptions)) {
                var page = login(browser);
                page.navigate(punchUrl);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("簽到")).click();
                if (!isMessageEqual(page, "簽到完成")) {
                    throw new ClockInFailedException("簽到失敗");
                }
            }
        }
    }

    public void clockOut() throws LoginFailedException, ClockOutFailedException {
        try (var playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
            try (var browser = playwright.chromium().launch(launchOptions)) {
                var page = login(browser);
                page.navigate(punchUrl);
                page.locator("#AttendWork").fill(jobDescription);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("簽退")).click();
                if (!isMessageEqual(page, "簽退完成")) {
                    throw new ClockOutFailedException("簽退失敗");
                }
            }
        }
    }

    private Page login(Browser browser) throws LoginFailedException {
        var context = browser.newContext();
        var page = context.newPage();
        page.navigate(LOGIN_URL);
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
    }

    public void loginCheck() throws LoginFailedException {
        try (var playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
            try (var browser = playwright.chromium().launch(launchOptions)) {
                login(browser);
            }
        }
    }

    private static boolean isErrorMessagesEqual(Page page, String msg) {
        return page.getByRole(AriaRole.PARAGRAPH).textContent().equals(msg);
    }

    private static boolean isMessageEqual(Page page, String msg) {
        return page.locator("#msg").textContent().equals(msg);
    }
}
