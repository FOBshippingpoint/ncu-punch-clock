package com.sdovan1.ncupunchclock.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

//    @Bean
//    public UserDetailsService userDetailsService() {
//        var user1 = User.withUsername("user1")
//                .password(passwordEncoder().encode("user1Pass"))
//                .roles("USER")
//                .build();
//        var user2 = User.withUsername("user2")
//                .password(passwordEncoder().encode("user2Pass"))
//                .roles("USER")
//                .build();
//        var admin = User.withUsername("admin")
//                .password(passwordEncoder().encode("adminPass"))
//                .roles("ADMIN")
//                .build();
//        return new InMemoryUserDetailsManager(user1, user2, admin);
////        return new MyUserDetailsService();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AesBytesEncryptor encryptor(@Value("${secret-key}") String secret) {
        return new AesBytesEncryptor(secret, "deadbeef");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(authorize -> authorize
////                .dispatcherTypeMatchers(FORWARD, ERROR).permitAll()
//                        .requestMatchers("/static/**", "/signup", "/about").permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
////                .requestMatchers("/db/**").access(allOf(hasAuthority('db'), hasRole('ADMIN')))
//                        .anyRequest().denyAll()
//        );

        http
                .authorizeHttpRequests((requests) -> requests
//                        .anyRequest().permitAll()
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers(antMatcher("/resources/**")).permitAll()
                                .requestMatchers(antMatcher("/")).permitAll()
                                .requestMatchers(antMatcher("/home")).permitAll()
                                .requestMatchers(antMatcher("/sign_up_success")).permitAll()
                                .requestMatchers(antMatcher("/schedules/**")).hasRole("USER")
                                .requestMatchers(antMatcher("/change_password")).hasRole("USER")
                                .requestMatchers(antMatcher("/change_make_webhooks")).hasRole("USER")
                                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                                .requestMatchers(antMatcher("/h2-console/**")).hasRole("ADMIN")
                                .requestMatchers(antMatcher("/sign_up")).anonymous()
                                .requestMatchers(antMatcher("/verify_account")).anonymous()
                                .requestMatchers(antMatcher("/sse/verify_account/*")).anonymous()
                                .requestMatchers(antMatcher("/login")).anonymous()
                                .anyRequest().authenticated()
                )
                .formLogin((form) -> form.loginPage("/login"))
                .logout((logout) -> logout.permitAll());
        http.csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/h2-console/**")));
        http.headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable());
        return http.build();
    }
}