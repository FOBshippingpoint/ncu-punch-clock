package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.bot.PunchAgent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PunchAgentFactory {
    @Value("${chrome-driver-binary-path}")
    private String chromeDriverBinaryPath;

    @Value("${web-driver-headless}")
    private boolean isWebDriverHeadless;
    public PunchAgent create(String username, String password) {
        var driver = createChromeDriver();
        return new PunchAgent(driver, null, username, password);
    }

    public PunchAgent create(String partTimeUsuallyId, String username, String password) {
        var driver = createChromeDriver();
        return new PunchAgent(driver, partTimeUsuallyId, username, password);
    }

    private WebDriver createChromeDriver() {
        var option = new ChromeOptions();
        if (isWebDriverHeadless) {
            option.addArguments("--headless");
        }
        if (chromeDriverBinaryPath != null) {
            option.setBinary(chromeDriverBinaryPath);
        }
        return new ChromeDriver(option);
    }
}
