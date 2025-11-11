package com.example.onlineshoppingapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a line item within a specific Order, capturing product and historical pricing.
 */
@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Links to the specific Order

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Links to the Product that was purchased

    // --- Order Details ---

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // The quantity of this product ordered in the transaction

    // --- Historical Price Captures (Prefixes Removed) ---

    // Captures the retail price at the moment the order was placed.
    @Column(name = "retail_price", nullable = false)
    private Double retailPrice;

    // Captures the wholesale price at the moment the order was placed (for profit calculation).
    @Column(name = "wholesale_price", nullable = false)
    private Double wholesalePrice;
}