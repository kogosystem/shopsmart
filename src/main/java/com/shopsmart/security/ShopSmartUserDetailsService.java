package com.shopsmart.security;

import com.shopsmart.entity.Role;
import com.shopsmart.entity.User;
import com.shopsmart.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tells Spring Security how to load a user from OUR database during login.
 *
 * Spring Security calls loadUserByUsername() when someone submits the login
 * form. We look the email up in the users table and convert the stored roles
 * ("ROLE_CUSTOMER" / "ROLE_ADMIN") into authorities that Spring understands.
 *
 * The password we return is the BCrypt HASH - Spring compares it with the typed
 * password using the BCryptPasswordEncoder bean, so the plain password is never
 * stored anywhere.
 */
@Service
public class ShopSmartUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ShopSmartUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + email));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // Return our richer principal so controllers can read the user id directly.
        return new ShopSmartUserPrincipal(user, authorities);
    }
}
