package com.example.onlineshoppingapp.domain;

import com.example.onlineshoppingapp.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a product available for sale in the online shopping application.
 */
@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    @JsonView(Views.PublicView.class) // ID is visible to everyone
    private Integer id;

    @Column(name = "name", nullable = false)
    @JsonView(Views.PublicView.class) // Name is visible to everyone
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonView(Views.PublicView.class) // Description is visible to everyone
    private String description;

    @Column(name = "wholesale_price", nullable = false)
    @JsonView(Views.AdminView.class) // Only visible in Admin View
    private Double wholesalePrice;

    @Column(name = "retail_price", nullable = false)
    @JsonView(Views.PublicView.class) // Retail Price is visible to everyone
    private Double retailPrice;

    @Column(name = "quantity", nullable = false)
    @JsonView(Views.AdminView.class) // Only visible in Admin View (actual stock)
    private Integer quantity;
}