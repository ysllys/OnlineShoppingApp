package com.example.onlineshoppingapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents the linking table for the User-Product Watchlist relationship,
 * using a single surrogate primary key.
 */
@Entity
@Table(name = "Watchlist")
@Getter
@Setter
@NoArgsConstructor
public class Watchlist {

    @Id // Single Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watchlist_id")
    private Integer id;

    // --- Relationships ---

    // Foreign Key to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Foreign Key to Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Constructor for easy creation
    public Watchlist(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}