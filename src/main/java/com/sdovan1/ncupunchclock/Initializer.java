package com.sdovan1.ncupunchclock;

import com.sdovan1.ncupunchclock.passcode.Passcode;
import com.sdovan1.ncupunchclock.passcode.PasscodeRepository;
import com.sdovan1.ncupunchclock.schedule.Punch;
import com.sdovan1.ncupunchclock.schedule.PunchRepository;
import com.sdovan1.ncupunchclock.schedule.PunchScheduler;
import com.sdovan1.ncupunchclock.user.User;
import com.sdovan1.ncupunchclock.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Initializer {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PunchRepository punchRepository;
    @Autowired
    private PasscodeRepository passcodeRepository;
    @Autowired
    private PunchScheduler punchScheduler;
    @Bean
    public CommandLineRunner initialize(@Value("${admin-password}") String adminPassword, @Value("${passcode-list}") String[] passcodeList) {
        return args -> {
            log.info("===Initializing NCU Punch Clock===");
            // Initialize admin
            log.info("===Initializing admin===");
            var admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(admin);
                log.info("Admin created");
            } else {
                log.info("Admin already exists");
            }

//            var user1 = new User();
//            user1.setUsername("user1");
//            user1.setPassword(passwordEncoder.encode("a"));
//            if (userRepository.findByUsername("user1").isEmpty()) {
//                userRepository.save(user1);
//            }

            // Initialize existing punches
            log.info("===Initializing existing punches===");
            var punches = punchRepository.findAll();
            // Set punches to expired based on current time and save
            punches.forEach(Punch::resetStatus);
            punchRepository.saveAll(punches);
            punchScheduler.scheduleAll(punches);

            // Initialize passcodes
            log.info("===Initializing passcodes===");
            for (String passcode : passcodeList) {
                var passcodeEntity = new Passcode(passcode);
                if (passcodeRepository.findByPasscode(passcode).isEmpty()) {
                    passcodeRepository.save(passcodeEntity);
                    log.info("Passcode {} created", passcode);
                } else {
                    log.info("Passcode {} already exists", passcode);
                }
            }
            log.info("===Initialization complete===");
        };
    }
}
