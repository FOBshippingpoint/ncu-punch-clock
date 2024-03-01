package com.sdovan1.ncupunchclock.bot;

import com.sdovan1.ncupunchclock.schedule.ClockInFailedException;
import com.sdovan1.ncupunchclock.schedule.ClockOutFailedException;
import com.sdovan1.ncupunchclock.schedule.LoginFailedException;

public interface PunchAgent {
    void loginCheck() throws LoginFailedException;
    void clockIn() throws LoginFailedException, ClockInFailedException;
    void clockOut() throws LoginFailedException, ClockOutFailedException;
}
