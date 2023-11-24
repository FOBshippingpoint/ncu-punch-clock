package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.schedule.Punch;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PunchValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Punch.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var punch = (Punch) target;
        if (punch.getClockOutTime().isBefore(punch.getClockInTime())) {
            errors.rejectValue("clockInTime", "clockInTime", "上班時間必須早於下班時間");
        }
    }
}
