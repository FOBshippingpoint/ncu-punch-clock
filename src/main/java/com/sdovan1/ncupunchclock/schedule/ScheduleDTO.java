package com.sdovan1.ncupunchclock.schedule;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ScheduleDTO {

    @Min(value = 8, message = "上班時間必須介於8-17時之間")
    @Max(value = 17, message = "上班時間必須介於8-17時之間")
    private Integer clockInTime;

    @Min(value = 8, message = "下班時間必須介於8-17時之間")
    @Max(value = 17, message = "下班時間必須介於8-17時之間")
    private Integer clockOutTime;

    @Min(value = 1, message = "開始日期必須介於1-31日之間")
    @Max(value = 31, message = "開始日期必須介於1-31日之間")
    private Integer startDate;

    @Min(value = 1, message = "結束日期必須介於1-31日之間")
    @Max(value = 31, message = "結束日期必須介於1-31日之間")
    private Integer endDate;

    @NotEmpty(message = "必須選擇至少一個月份")
    private List<YearMonth> yearMonths;

    @NotEmpty(message = "簽到工作/計畫網址不得為空")
    @Pattern(regexp = "^.*ParttimeUsuallyId=([^&]+).*$", message = "簽到工作/計畫網址必須包含ParttimeUsuallyId參數")
    private String partTimeUrl;

    @NotNull(message = "工作內容不得為空")
    private String jobDescription;

    private String jobName;

    public List<Punch> toPunchList() {
        var punches = new ArrayList<Punch>();
        for (var yearMonth : yearMonths) {
            var date = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), startDate);
            while (date.getDayOfMonth() <= endDate) {
                var punch = Punch.builder()
                        .clockInTime(Instant.from(LocalDateTime.of(date, LocalTime.of(clockInTime, 0)).atZone(Punch.TIME_ZONE)))
                        // Add 2 minutes to clock out time to ensure (clock out - clock in time) > 1hr
                        .clockOutTime(Instant.from(LocalDateTime.of(date, LocalTime.of(clockOutTime, 2)).atZone(Punch.TIME_ZONE)))
                        .jobName(jobName)
                        .jobDescription(jobDescription)
                        .partTimeUsuallyId(Punch.getPartTimeUsuallyIdFromUrl(partTimeUrl))
                        .status(Punch.Status.PENDING)
                        .build();
                punches.add(punch);
                date = date.plusDays(1);
            }
        }
        return punches;
    }

    public String getJobDescription() {
        if (jobDescription == null || jobDescription.isEmpty()) {
            return " ";
        }
        return jobDescription;
    }
}
