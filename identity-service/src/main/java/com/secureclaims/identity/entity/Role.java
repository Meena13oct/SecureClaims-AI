package com.secureclaims.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents a security role in the system (e.g., USER, ADMIN).
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Entity
@Table(name = "roles", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    public Role(final String name) {
        this.name = name;
    }
}
