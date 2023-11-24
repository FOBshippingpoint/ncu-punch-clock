package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class IftttKeyService {
    @Autowired
    private IftttKeyRepository iftttKeyRepository;

    public boolean checkIfUserIsOwner(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        return iftttKeyRepository.findByUser(user).isPresent();
    }
}
