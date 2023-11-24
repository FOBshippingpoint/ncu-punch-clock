package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.schedule.ScheduleDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ScheduleDTOValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ScheduleDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var scheduleDTO = (ScheduleDTO) target;
        if (scheduleDTO.getClockInTime() != null && scheduleDTO.getClockOutTime() != null && scheduleDTO.getClockInTime() >= scheduleDTO.getClockOutTime()) {
            errors.rejectValue("clockInTime", "clockInTime", "上班時間必須早於下班時間");
        }
        if (scheduleDTO.getStartDate() != null && scheduleDTO.getEndDate() != null && scheduleDTO.getStartDate() >= scheduleDTO.getEndDate()) {
            errors.rejectValue("startDate", "startDate", "每月開始日期必須早於每月結束日期");
        }

        if (scheduleDTO.getYearMonths() != null && scheduleDTO.getStartDate() != null && scheduleDTO.getEndDate() != null) {
            for (var month : scheduleDTO.getYearMonths()) {
                if (!month.isValidDay(scheduleDTO.getStartDate()) || !month.isValidDay(scheduleDTO.getEndDate())) {
                    errors.rejectValue("yearMonths", "yearMonths", "選擇的月份必須包含開始日期與結束日期");
                }
            }
        }
    }
}
