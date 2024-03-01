package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.schedule.Punch;
import com.sdovan1.ncupunchclock.schedule.PunchEvent;
import com.sdovan1.ncupunchclock.user.User;
import lombok.Getter;

@Getter
public class WebhooksPunchEvent extends PunchEvent {
    private final User user;
    public WebhooksPunchEvent(Punch punch) {
        super(punch);
        this.user = punch.getUser();
    }
}
