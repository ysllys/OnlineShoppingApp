package com.example.onlineshoppingapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a customer's order in the online shopping application.
 */
@Entity
@Table(name = "Order") // Using "Order" as the table name
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    // Foreign Key relationship to the User who placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Alternative: If you prefer to store just the FK ID and manage the relationship separately:
    // @Column(name = "user_id", nullable = false)
    // private Integer userId;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime; // Using LocalDateTime for placement time

    @Enumerated(EnumType.STRING) // Stores the enum value as a String ("PROCESSING", "COMPLETED", etc.)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    // ADD THIS FIELD: Defines the relationship to OrderItem
    @OneToMany(mappedBy = "order", // 'order' is the field name in the OrderItem entity
            cascade = CascadeType.ALL, // Cascade operations (e.g., if Order is deleted, delete items)
            fetch = FetchType.LAZY) // Load items only when explicitly accessed
    private List<OrderItem> items;
    // --- Enum Definition ---

    // Note: This Enum should be defined separately in your model package,
    // but is shown here for clarity of the attribute type.
    public enum OrderStatus {
        PROCESSING,
        COMPLETED,
        CANCELED
    }
}
