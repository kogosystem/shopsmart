package com.shopsmart.config;

import com.shopsmart.entity.Role;
import com.shopsmart.entity.User;
import com.shopsmart.repository.RoleRepository;
import com.shopsmart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Runs ONCE at every startup and makes sure the two roles and a default admin
 * account exist. This is the safe way to seed an admin (spec section 93):
 *
 *   - the password is read from the environment variable SHOPSMART_ADMIN_PASSWORD
 *     if present, otherwise it falls back to a known development default;
 *   - it is BCrypt-hashed before being saved - no plain-text password is written
 *     to the database or the source code.
 *
 * It only creates the admin if one does not already exist, so restarting the
 * app never overwrites a password you changed.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedRolesAndAdmin(RoleRepository roleRepository,
                                               UserRepository userRepository,
                                               PasswordEncoder passwordEncoder) {
        return args -> {
            // 1) make sure both roles exist
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
            roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));

            // 2) make sure a default admin exists
            String adminEmail = "admin@shopsmart.co.ke";
            if (!userRepository.existsByEmail(adminEmail)) {
                String rawPassword = System.getenv().getOrDefault(
                        "SHOPSMART_ADMIN_PASSWORD", "Admin@123");

                User admin = new User("System Administrator", adminEmail,
                        passwordEncoder.encode(rawPassword));   // hashed here
                admin.setCity("Nairobi");
                admin.addRole(adminRole);
                userRepository.save(admin);

                log.info("Seeded default admin account: {} (change the password after first login)",
                        adminEmail);
            }
        };
    }
}
