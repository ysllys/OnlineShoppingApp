package com.example.onlineshoppingapp.dao;

import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.domain.OrderItem;
import com.example.onlineshoppingapp.domain.Order.OrderStatus;
import com.example.onlineshoppingapp.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // --- Custom Exception ---
    // Note: This should ideally be defined in the service layer or a dedicated exception package
    public static class NotEnoughInventoryException extends RuntimeException {
        public NotEnoughInventoryException(String message) {
            super(message);
        }
    }

    // --- POST: Place an order ---

    /**
     * Places a new order, creates OrderItems, and deducts product stock.
     * This method combines persistence logic with inventory manipulation.
     * @param order The new Order entity (with status PROCESSING).
     * @param items The list of OrderItems to persist.
     * @throws NotEnoughInventoryException if stock is insufficient.
     * @return The persisted Order entity.
     */
    @Transactional
    public Order placeOrder(Order order, List<OrderItem> items) {

        // 1. Check and Deduct Inventory
        for (OrderItem item : items) {
            Product product = item.getProduct();
            int purchasedQuantity = item.getQuantity();

            if (product.getQuantity() < purchasedQuantity) {
                // Throw custom exception if stock is insufficient
                throw new NotEnoughInventoryException("Not enough stock for product ID " + product.getId() +
                        ". Available: " + product.getQuantity() +
                        ", Requested: " + purchasedQuantity);
            }
            // Deduct stock directly on the managed Product entity
            product.setQuantity(product.getQuantity() - purchasedQuantity);
            entityManager.merge(product); // Explicitly merge the change
        }

        // 2. Persist Order and OrderItems
        // Order entity must be persisted first to get its ID
        order.setStatus(OrderStatus.PROCESSING);
        entityManager.persist(order);

        // Associate and persist OrderItems (which capture historical prices)
        for (OrderItem item : items) {
            item.setOrder(order); // Link the item to the newly persisted order
            entityManager.persist(item);
        }

        return order;
    }

    // --- PATCH: Update Order Status (Cancel/Complete) ---

    /**
     * Updates the status of an order and manages stock adjustments based on status change.
     * This method handles both user and seller cancellation/completion.
     * @param orderId The ID of the order to update.
     * @param newStatus The target status (CANCELED or COMPLETED).
     * @throws IllegalStateException if the status change is invalid.
     * @return The updated Order entity.
     */
    @Transactional
    public Order updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = entityManager.find(Order.class, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        OrderStatus currentStatus = order.getStatus();

        // 1. Validate Status Change
        if (newStatus == OrderStatus.CANCELED) {
            // Cannot cancel a Completed order
            if (currentStatus == OrderStatus.COMPLETED) {
                throw new IllegalStateException("Completed orders cannot be canceled.");
            }
        } else if (newStatus == OrderStatus.COMPLETED) {
            // Cannot complete a Canceled order
            if (currentStatus == OrderStatus.CANCELED) {
                throw new IllegalStateException("Canceled orders cannot be completed.");
            }
        } else {
            throw new IllegalArgumentException("Invalid status update: " + newStatus);
        }

        // 2. Adjust Inventory for CANCELLATION
        // Only increment stock if the order is moving FROM PROCESSING to CANCELED
        if (currentStatus == OrderStatus.PROCESSING && newStatus == OrderStatus.CANCELED) {

            // HQL to fetch order items associated with the order
            String hql = "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId";
            List<OrderItem> items = entityManager.createQuery(hql, OrderItem.class)
                    .setParameter("orderId", orderId)
                    .getResultList();

            for (OrderItem item : items) {
                Product product = item.getProduct();
                // Ensure the Product entity is managed before updating stock
                product = entityManager.find(Product.class, product.getId());

                // Increment stock by the quantity originally purchased
                product.setQuantity(product.getQuantity() + item.getQuantity());
                entityManager.merge(product);
            }
        }

        // 3. Update Status
        order.setStatus(newStatus);
        return entityManager.merge(order);
    }

    // --- GET methods ---

    /**
     * Retrieves all orders placed by a specific user (GET all orders by that user).
     * @param userId The ID of the user.
     * @return A list of Order entities.
     */
    public List<Order> findAllByUserId(Integer userId) {
        String hql = "SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderTime DESC";
        return entityManager.createQuery(hql, Order.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Retrieves a specific order and its associated items (GET order detail).
     * Uses JOIN FETCH to load OrderItems in a single query (prevents N+1 problem).
     * @param orderId The ID of the order.
     * @return An Optional containing the Order, or empty if not found.
     */
    public Optional<Order> findDetailById(Integer orderId) {
        // HQL with JOIN FETCH ensures OrderItems are loaded immediately.
        String hql = "SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :orderId";

        try {
            Order order = entityManager.createQuery(hql, Order.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
            return Optional.of(order);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    // --- Helper Class (Define this outside the DAO or as a separate file) ---
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}