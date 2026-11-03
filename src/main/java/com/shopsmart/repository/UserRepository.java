package com.shopsmart.repository;

import com.shopsmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/** Database access for users (both customers and admins). */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used by login and by the duplicate-email check during registration. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * All users who hold a given role. The admin "view customers" screen uses
     * this with "ROLE_CUSTOMER" so administrators do not appear in that list.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY u.fullName")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    /** Count of users holding a role - used for the dashboard "Total Customers" card. */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
}
