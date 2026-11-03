package com.shopsmart.security;

import com.shopsmart.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * A richer UserDetails than Spring's default: it also carries the database id
 * and display name of the logged-in user.
 *
 * Controllers need that id all the time ("show THIS customer's cart"), and
 * reading it from the principal avoids an extra lookup by email on every request.
 */
public class ShopSmartUserPrincipal implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public ShopSmartUserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.active = user.isActive();
        this.authorities = authorities;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
