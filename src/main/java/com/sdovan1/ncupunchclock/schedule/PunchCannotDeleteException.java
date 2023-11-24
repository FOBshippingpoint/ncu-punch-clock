package com.sdovan1.ncupunchclock.schedule;

public class PunchCannotDeleteException extends RuntimeException{
    public PunchCannotDeleteException(String message) {
        super(message);
    }
}
