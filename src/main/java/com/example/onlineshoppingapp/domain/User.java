package com.example.onlineshoppingapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a User (Customer or Admin) in the online shopping application.
 */
@Entity
@Table(name = "User")
@Getter // Generates all getters
@Setter // Generates all setters
@NoArgsConstructor // Generates the default (no-argument) constructor required by JPA
@AllArgsConstructor // Generates a constructor with all fields
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Stores the hashed password

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false; // Default: FALSE, as only users (not admins) register initially.

}