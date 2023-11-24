package com.sdovan1.ncupunchclock.bot;

import com.sdovan1.ncupunchclock.schedule.Punch;
import com.sdovan1.ncupunchclock.schedule.PunchClockInFailedException;
import com.sdovan1.ncupunchclock.schedule.PunchLoginFailedException;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;


public class PunchAgent {

    private static final String LOGIN_URL = "https://portal.ncu.edu.tw/login";
    private final String url;
    private final String username;
    private final String password;

    @Getter
    private final WebDriver driver;

    private boolean isLoggedIn = false;

    private String jobDescription;

    public static final String MSG_USERNAME_OR_PASSWORD_WRONG = "您輸入的密碼不正確";
    public static final String MSG_PLEASE_CHECK_CAPTCHA = "請勾我不是機器人";

    public PunchAgent(WebDriver driver, String partTimeUsuallyId, String username, String password) {
        this.driver = driver;
        this.url = Punch.partTimeUsuallyIdToUrl(partTimeUsuallyId);
        this.username = username;
        this.password = password;
    }

    public LoginRequiredAction login() {
        if (isLoggedIn) {
            return new LoginRequiredAction();
        }
        driver.get(LOGIN_URL);
        var usernameInput = driver.findElement(By.id("inputAccount"));
        usernameInput.sendKeys(username);

        var passwordInput = driver.findElement(By.id("inputPassword"));
        passwordInput.sendKeys(password);
        passwordInput.submit();

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        var wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(2))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(NoSuchElementException.class);

        try {
            wait.until(d -> {
                if (!d.getCurrentUrl().equals(LOGIN_URL)) {
                    return true;
                } else {
                    var errorMsg = d.findElement(By.cssSelector(".alert > .mb-0"));
                    if (errorMsg.isDisplayed()) {
                        if (errorMsg.getText().equals(MSG_USERNAME_OR_PASSWORD_WRONG)) {
                            throw new PunchLoginFailedException(PunchLoginFailedException.WRONG_USERNAME_OR_PASSWORD);
                        } else if (errorMsg.getText().equals(MSG_PLEASE_CHECK_CAPTCHA)) {
                            throw new PunchLoginFailedException(PunchLoginFailedException.FAILED_TO_PASS_CAPTCHA);
                        }
                        throw new PunchLoginFailedException(errorMsg.getText());
                    }
                    return false;
                }
            });
            isLoggedIn = true;
            return new LoginRequiredAction();
        } catch (TimeoutException e) {
            throw new PunchLoginFailedException(PunchLoginFailedException.TIMEOUT);
        }
    }

    public class LoginRequiredAction {
        public void clockIn() {
            driver.get(url);
            driver.findElement(By.cssSelector(".btn.btn-primary")).click();
            var wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofMinutes(1))
                    .pollingEvery(Duration.ofMillis(300))
                    .ignoring(WebDriverException.class);
            wait.until(d -> {
                var clockInBtn = d.findElement(By.id("signin"));
                clockInBtn.click();
                return true;
            });
            var result = isMsgEquals("簽到完成");
            if (!result) {
                throw new PunchClockInFailedException("Clock in failed");
            }
        }

        public void clockOut(String jobDescription) {
            driver.get(url);
            var wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofMinutes(1))
                    .pollingEvery(Duration.ofMillis(300))
                    .ignoring(WebDriverException.class);
            wait.until(d -> {
                driver.findElement(By.id("AttendWork"));
                // sendKeys does not work here, thus we use javascript
                ((JavascriptExecutor) driver).executeScript("document.getElementById('AttendWork').value = '" + jobDescription + "';");
                return true;
            });
            wait.until(d -> {
                var clockOutBtn = driver.findElement(By.id("signout"));
                clockOutBtn.click();
                return true;
            });

            var result = isMsgEquals("簽退完成");
            if (!result) {
                throw new PunchClockInFailedException("Clock in failed");
            }
        }

        public void clockOut() {
            if (jobDescription == null) {
                throw new PunchClockInFailedException("Job description is required");
            }
            clockOut(jobDescription);
        }

        private boolean isMsgEquals(String msg) {
            var wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofMinutes(1))
                    .pollingEvery(Duration.ofMillis(300))
                    .ignoring(WebDriverException.class);
            try {
                wait.until(d -> {
                    var msgElement = d.findElement(By.id("msg"));
                    return msgElement.getText().equals(msg);
                });
                return true;
            } catch (TimeoutException e) {
                return false;
            }
        }

    }
}

