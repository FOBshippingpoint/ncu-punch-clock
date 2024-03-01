package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.bot.PlaywrightAgent;
import com.sdovan1.ncupunchclock.bot.PunchAgent;
import org.springframework.stereotype.Component;

@Component
public class PunchAgentFactory {
    public PunchAgent create(String username, String password) {
        return new PlaywrightAgent(null, username, password, null);
    }

    public PunchAgent create(String partTimeUsuallyId, String username, String password, String jobDescription) {
        return new PlaywrightAgent(partTimeUsuallyId, username, password, jobDescription);
    }
}
