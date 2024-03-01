package com.sdovan1.ncupunchclock.schedule;
public class ClockOutFailedException extends RuntimeException {
    public ClockOutFailedException(String message) {
        super(message);
    }
}
