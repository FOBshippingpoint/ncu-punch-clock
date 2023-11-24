package com.sdovan1.ncupunchclock.user;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ChangePasswordDTOValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var changePasswordDTO = (ChangePasswordDTO) target;
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            errors.rejectValue("confirmNewPassword", "confirmNewPassword", "您輸入的兩個新密碼並不相符，請再試一次。");
        }
    }
}
