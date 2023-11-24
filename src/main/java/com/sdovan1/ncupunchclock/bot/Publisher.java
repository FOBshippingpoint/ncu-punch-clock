package com.sdovan1.ncupunchclock.bot;

import com.sdovan1.ncupunchclock.schedule.PunchEvent;

public interface Publisher {
    <T extends PunchEvent> void trigger(T punchEvent);
}
