package com.sdovan1.ncupunchclock.schedule;
public class ClockInFailedException extends RuntimeException {
    public ClockInFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClockInFailedException(String message) {
        super(message);
    }
}
