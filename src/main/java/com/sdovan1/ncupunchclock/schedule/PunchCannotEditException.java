package com.sdovan1.ncupunchclock.schedule;

public class PunchCannotEditException extends RuntimeException{
    public PunchCannotEditException(String message) {
        super(message);
    }
}
