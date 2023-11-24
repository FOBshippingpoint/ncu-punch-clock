package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class IftttKey {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String iftttWebhooksKey;

    @OneToOne
    private User user;
}
