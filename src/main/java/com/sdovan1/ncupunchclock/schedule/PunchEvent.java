package com.sdovan1.ncupunchclock.schedule;

import lombok.Getter;

@Getter
public class PunchEvent {
    String jobName;
    String jobDescription;
    String partTimeUsuallyUrl;
    Punch.ClockInOutStatus status;

    public String getSummary() {
        String status;
        if (getStatus() == Punch.ClockInOutStatus.CLOCK_IN_SUCCESS) {
            status = getStatus() + "✨";
        } else if (getStatus() == Punch.ClockInOutStatus.CLOCK_OUT_SUCCESS) {
            status = getStatus() + "🎉";
        } else if (getStatus() == Punch.ClockInOutStatus.CLOCK_IN_FAILED) {
            status = getStatus() + "😭";
        } else if (getStatus() == Punch.ClockInOutStatus.CLOCK_OUT_FAILED) {
            status = getStatus() + "😭";
        } else {
            status = getStatus().toString();
        }
        return new StringBuilder()
                .append("【NCU打卡魔法師通知】\n")
                .append(getJobName())
                .append(" - ")
                .append(status)
                .append("\n工作內容：")
                .append(getJobDescription())
                .append("\n簽到網址：")
                .append(getPartTimeUsuallyUrl())
                .toString();
    }

    public PunchEvent(Punch punch) {
        this.jobName = punch.getJobName();
        this.jobDescription = punch.getJobDescription();
        this.partTimeUsuallyUrl = punch.getPartTimeUsuallyUrl();
        this.status = punch.getStatus();
    }
}
