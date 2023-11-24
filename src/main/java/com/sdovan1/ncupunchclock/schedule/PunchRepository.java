package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.user.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface PunchRepository extends ListCrudRepository<Punch, Long> {
    List<Punch> findByUserOrderByStatusAscClockInTime(User user);
    List<Punch> findByUser_UsernameOrderByStatusAscClockInTime(String username);
    List<Punch> findAllByOrderByStatusAscClockInTime();
}
