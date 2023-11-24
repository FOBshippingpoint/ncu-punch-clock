package com.sdovan1.ncupunchclock.user;

import com.sdovan1.ncupunchclock.schedule.Punch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ToString.Include
    private Long id;

    @ToString.Include
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String pass;

    @Transient
    private String confirmPassword;

    @Transient
    private String passcode;

    @OneToMany
    @OrderBy("clockInTime")
    private List<Punch> punches;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.pass = user.getPass();
        this.punches = user.getPunches();
    }

}
