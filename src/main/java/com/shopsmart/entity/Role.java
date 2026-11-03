package com.shopsmart.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Lookup table of authorities. Names are stored WITH the ROLE_ prefix. */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    /** Stored as "ROLE_ADMIN" or "ROLE_CUSTOMER" (Spring Security convention). */
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    public Role() { }

    public Role(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Roles live in a Set on User, so equals/hashCode matter.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hash(getClass()); }
}
