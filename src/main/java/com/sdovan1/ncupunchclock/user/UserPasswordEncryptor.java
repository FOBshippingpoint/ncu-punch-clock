package com.sdovan1.ncupunchclock.user;

import com.sdovan1.ncupunchclock.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordEncryptor {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BytesEncryptor encryptor;

    public void setPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        user.setPass(new String(Hex.encode(encryptor.encrypt(password.getBytes()))));
    }
}
