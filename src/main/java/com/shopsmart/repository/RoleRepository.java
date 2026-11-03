package com.shopsmart.repository;

import com.shopsmart.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Database access for roles (ROLE_ADMIN, ROLE_CUSTOMER). */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /** Used at startup to find-or-create the two roles the app needs. */
    Optional<Role> findByName(String name);
}
