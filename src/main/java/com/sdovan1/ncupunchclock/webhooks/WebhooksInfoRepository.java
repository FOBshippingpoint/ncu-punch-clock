package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WebhooksInfoRepository extends CrudRepository<WebhooksInfo, Long> {
    Optional<WebhooksInfo> findByUser(User user);
}
