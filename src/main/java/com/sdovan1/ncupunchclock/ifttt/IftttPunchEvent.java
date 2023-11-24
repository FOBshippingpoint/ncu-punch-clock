package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.schedule.Punch;
import com.sdovan1.ncupunchclock.schedule.PunchEvent;
import com.sdovan1.ncupunchclock.user.User;
import lombok.Getter;

@Getter
public class IftttPunchEvent extends PunchEvent {
    private final User user;
    public IftttPunchEvent(Punch punch) {
        super(punch);
        this.user = punch.getUser();
    }
}
