package com.sdovan1.ncupunchclock.schedule;
public class LoginFailedException extends RuntimeException {
    public static final String FAILED_TO_PASS_CAPTCHA = "無法通過機器人檢查";
    public static final String WRONG_USERNAME_OR_PASSWORD = "Portal帳號或密碼錯誤";
    public static final String TIMEOUT = "逾時";
    public LoginFailedException(String message) {
        super(message);
    }
}
