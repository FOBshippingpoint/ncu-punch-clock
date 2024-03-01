package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class WebhooksInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String info;

    @OneToOne
    private User user;
}
