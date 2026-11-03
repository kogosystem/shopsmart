package com.shopsmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Central Spring Security configuration.
 *
 * NOTE ON VERSION: this uses the Spring Security 7 Lambda DSL. The old
 * WebSecurityConfigurerAdapter / .and() / antMatchers() style was REMOVED in
 * Security 7, so this is the modern and correct form for Spring Boot 4.
 *
 * We deliberately do NOT declare a DaoAuthenticationProvider bean by hand.
 * Because we expose a UserDetailsService bean (ShopSmartUserDetailsService) and
 * a PasswordEncoder bean below, Spring Boot auto-wires exactly that provider for
 * us. Letting Boot do it keeps this class free of APIs that shifted between
 * Spring Security generations - one less thing to break on an upgrade.
 *
 * What it does:
 *   - hashes passwords with BCrypt (never stores plain text)
 *   - makes the home page, catalogue, login and register pages public
 *   - requires ROLE_ADMIN for everything under /admin/**
 *   - requires login for the customer areas
 *   - after login, sends admins to the dashboard and customers to their home
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize("hasRole('ADMIN')") style checks
public class SecurityConfig {

    /** BCrypt with the default strength. Used to hash and to verify passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * After a successful login, route by role: admins to the admin dashboard,
     * everyone else to the customer home page.
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(request.getContextPath() + (admin ? "/admin/dashboard" : "/home"));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler successHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ---- public pages ----
                .requestMatchers("/", "/products/**", "/category/**",
                                 "/login", "/register", "/error").permitAll()
                // ---- static resources (CSS, JS, images) ----
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // ---- admin area: only ROLE_ADMIN ----
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // ---- customer area: any logged-in customer ----
                .requestMatchers("/cart/**", "/checkout/**", "/orders/**",
                                 "/recommendations/**", "/feedback/**", "/profile/**")
                    .hasRole("CUSTOMER")
                // ---- anything else needs a login ----
                .anyRequest().authenticated()
            )
            // ---- form login (our own Thymeleaf page) ----
            .formLogin(form -> form
                .loginPage("/login")                 // GET shows the page
                .loginProcessingUrl("/login")        // POST is processed by Spring Security
                .successHandler(successHandler)      // role-based redirect (see bean above)
                .failureUrl("/login?error")          // ?error lets the page show a message
                .permitAll()
            )
            // ---- logout ----
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
            // CSRF protection stays ON (the Spring default). Thymeleaf adds the
            // hidden CSRF token to every th:action form automatically, so all our
            // POST forms (login, add-to-cart, checkout, feedback, admin CRUD) work.

        return http.build();
    }
}
