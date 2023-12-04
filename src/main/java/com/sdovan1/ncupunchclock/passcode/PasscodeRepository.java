package com.sdovan1.ncupunchclock.passcode;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PasscodeRepository extends CrudRepository<Passcode, Long> {
    Optional<Passcode> findByPasscode(String passcode);

    void deleteByPasscode(String passcode);
}