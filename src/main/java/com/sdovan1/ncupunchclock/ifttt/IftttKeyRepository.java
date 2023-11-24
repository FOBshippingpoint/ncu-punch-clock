package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IftttKeyRepository extends CrudRepository<IftttKey, Long> {
    Optional<IftttKey> findByUser(User user);
}
