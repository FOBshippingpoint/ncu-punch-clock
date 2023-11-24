package com.sdovan1.ncupunchclock.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChangePasswordDTO {
    String oldPassword;
    String newPassword;
    String confirmNewPassword;
}
