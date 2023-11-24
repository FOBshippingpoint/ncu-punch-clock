package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.ZoneId;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Punch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant clockInTime;

    @Column(nullable = false)
    private Instant clockOutTime;

    @Column(nullable = false)
    private String partTimeUsuallyId;

    private String jobName;

    @Column(nullable = false)
    private String jobDescription;

    @Enumerated(EnumType.ORDINAL)
    private ClockInOutStatus status;

    @Transient
    public static final ZoneId TIME_ZONE = ZoneId.of("Asia/Taipei");

    public static enum ClockInOutStatus {
        PENDING("待簽到"),
        EXPIRED("已過期"),
        CLOCK_IN_SUCCESS("簽到成功"),
        CLOCK_OUT_SUCCESS("簽退成功"),
        CLOCK_IN_FAILED("簽到失敗"),
        CLOCK_OUT_FAILED("簽退失敗");

        final String name;

        ClockInOutStatus(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static String partTimeUsuallyIdToUrl(String partTimeUsuallyId) {
        return "https://cis.ncu.edu.tw/HumanSys/student/stdSignIn/create?ParttimeUsuallyId=" + partTimeUsuallyId;
    }

    public String getPartTimeUsuallyUrl() {
        return partTimeUsuallyIdToUrl(partTimeUsuallyId);
    }

    public void resetStatus() {
        if (status == ClockInOutStatus.PENDING && isAfterClockInTime() && isAfterClockOutTime()) {
            this.status = ClockInOutStatus.EXPIRED;
        }
    }

    public String getJobName() {
        if (jobName == null || jobName.isEmpty()) {
            return partTimeUsuallyId;
        } else {
            return jobName;
        }
    }

    public boolean isAfterClockInTime() {
        return Instant.now().isAfter(clockInTime);
    }

    public boolean isAfterClockOutTime() {
        return Instant.now().isAfter(clockOutTime);
    }

    public boolean isEditable() {
        return getStatus() == ClockInOutStatus.PENDING;
    }

    public static String getPartTimeUsuallyIdFromUrl(String partTimeUrl) {
        var pattern = java.util.regex.Pattern.compile("ParttimeUsuallyId=([^&]+)");
        var matcher = pattern.matcher(partTimeUrl);
        String partTimeUsuallyId;
        if (matcher.find()) {
            partTimeUsuallyId = matcher.group(1);
        } else {
            throw new RuntimeException("簽到工作/計畫網址必須包含ParttimeUsuallyId參數");
        }
        return partTimeUsuallyId;
    }
}
