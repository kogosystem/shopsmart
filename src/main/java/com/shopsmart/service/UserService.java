package com.shopsmart.service;

import com.shopsmart.dto.ProfileForm;
import com.shopsmart.dto.RegisterForm;
import com.shopsmart.entity.Role;
import com.shopsmart.entity.User;
import com.shopsmart.exception.BusinessException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.RoleRepository;
import com.shopsmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Registration, profile updates and the customer lists the admin screens use. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new CUSTOMER account.
     * Steps: reject duplicate emails, check the two passwords match, HASH the
     * password, attach ROLE_CUSTOMER (never an admin role from the browser).
     */
    @Transactional
    public User register(RegisterForm form) {
        if (!form.passwordsMatch()) {
            throw new BusinessException("The two passwords do not match.");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new BusinessException("An account with that email already exists.");
        }

        User user = new User();
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        // BCrypt hash - the plain password is never stored.
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setCity(form.getCity());

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER missing - was DataInitializer run?"));
        user.addRole(customerRole);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    /** Update only the safe profile fields. */
    @Transactional
    public void updateProfile(Long userId, ProfileForm form) {
        User user = getById(userId);
        user.setFullName(form.getFullName());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setCity(form.getCity());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> allCustomers() {
        return userRepository.findAllByRoleName("ROLE_CUSTOMER");
    }

    @Transactional(readOnly = true)
    public long countCustomers() {
        return userRepository.countByRoleName("ROLE_CUSTOMER");
    }
}
