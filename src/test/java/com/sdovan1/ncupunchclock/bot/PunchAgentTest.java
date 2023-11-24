package com.sdovan1.ncupunchclock.bot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.assertj.core.api.Assertions.assertThatNoException;


@Disabled
class PunchAgentTest {
    WebDriver driver;

    // Replace with your own id, username and password before testing.
    private static final String PART_TIME_USUALLY_ID = "12345";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
    }

    @Test
    void testLogin() {
        var agent = new PunchAgent(driver, PART_TIME_USUALLY_ID, USERNAME, PASSWORD);
        assertThatNoException().isThrownBy(agent::login);
    }

    @Test
    void testClockInClockOut() {
        var agent = new PunchAgent(driver, PART_TIME_USUALLY_ID, USERNAME, PASSWORD);
        assertThatNoException().isThrownBy(() -> {
            agent.login().clockIn();
            agent.login().clockOut("test");
        });
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

}