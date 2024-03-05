package com.sdovan1.ncupunchclock.schedule;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@Builder
public class PunchDTO {
    public PunchDTO(String partTimeUsuallyUrl, String jobDescription, Integer clockInTime, Integer clockOutTime, LocalDate date, String jobName) {
        this.partTimeUsuallyUrl = partTimeUsuallyUrl;
        this.jobDescription = jobDescription;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.date = date;
        this.jobName = jobName;
    }

    private Long id;

    @NotEmpty(message = "簽到工作/計畫網址不得為空")
    @Pattern(regexp = "^.*ParttimeUsuallyId=[^&]+.*$", message = "簽到工作/計畫網址必須包含ParttimeUsuallyId參數")
    private String partTimeUsuallyUrl;

    @NotNull(message = "工作內容不得為空")
    private String jobDescription;

    @Min(value = 8, message = "上班時間必須介於8-17時之間")
    @Max(value = 17, message = "上班時間必須介於8-17時之間")
    private Integer clockInTime;

    @Min(value = 8, message = "下班時間必須介於8-17時之間")
    @Max(value = 17, message = "下班時間必須介於8-17時之間")
    private Integer clockOutTime;

    @NotNull(message = "日期不得爲空")
    private LocalDate date;

    private String jobName;
    private Punch.Status status;

    public String getPartTimeUsuallyIdFromUrl() {
        return Punch.getPartTimeUsuallyIdFromUrl(partTimeUsuallyUrl);
    }

    public PunchDTO(Punch punch) {
        this.id = punch.getId();
        this.partTimeUsuallyUrl = Punch.partTimeUsuallyIdToUrl(punch.getPartTimeUsuallyId());
        this.jobDescription = punch.getJobDescription();
        this.clockInTime = punch.getClockInTime().atZone(Punch.TIME_ZONE).getHour();
        this.clockOutTime = punch.getClockOutTime().atZone(Punch.TIME_ZONE).getHour();
        this.date = punch.getClockInTime().atZone(Punch.TIME_ZONE).toLocalDate();
        this.jobName = punch.getJobName();
        this.status = punch.getStatus();
    }

    public Punch toPunch() {
        var clockInTime = Instant.from(
                LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), this.clockInTime, 0).atZone(Punch.TIME_ZONE)
        );
        // Add 2 minutes to clock out time to ensure (clock out - clock in time) > 1hr
        var clockOutTime = Instant.from(
                LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), this.clockOutTime, 2).atZone(Punch.TIME_ZONE)
        );

        return Punch.builder()
                .partTimeUsuallyId(getPartTimeUsuallyIdFromUrl())
                .jobName(getJobName())
                .jobDescription(getJobDescription())
                .clockInTime(clockInTime)
                .clockOutTime(clockOutTime)
                .status(Punch.Status.PENDING)
                .build();
    }

    @Override
    public String toString() {
        return "[" + status + "]" + DateTimeFormatter.ofPattern("yyyy/MM/dd").format(date) + "_" + clockInTime + "-" + clockOutTime + "@" + jobName;
    }

}
