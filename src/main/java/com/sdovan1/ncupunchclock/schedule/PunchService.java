package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("punchService")
public class PunchService {
    @Autowired
    private PunchRepository repository;

    public boolean checkIfUserIsOwner(Long id, CustomUserDetails user) {
        if (user.isAdmin()) {
            return true;
        }
        var result = repository.findById(id).orElseThrow();
        return result.getUser().getUsername().equals(user.getUsername());
    }
}
