package com.sdovan1.ncupunchclock.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CustomUserDetails extends User implements UserDetails {

    private static final List<GrantedAuthority> ROLE_USER = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("ROLE_USER"));
    private static final List<GrantedAuthority> ROLE_ADMIN = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"));

    @Getter
    private final User user;

    private final String[] adminList;

    public CustomUserDetails(User customUser,  String[] adminList) {
        super(customUser);
        this.user = customUser;
        this.adminList = adminList;
    }

    public CustomUserDetails(User customUser) {
        super(customUser);
        this.user = customUser;
        this.adminList = null;
    }

    public boolean isAdmin() {
        return getAuthorities() == ROLE_ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (adminList != null) {
            for (String admin : adminList) {
                if (getUsername().equals(admin)) {
                    return ROLE_ADMIN;
                }
            }
        }
        return ROLE_USER;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
