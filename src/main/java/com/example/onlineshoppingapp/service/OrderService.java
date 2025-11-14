package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.OrderDAO;
import com.example.onlineshoppingapp.dao.OrderDAO.NotEnoughInventoryException;
import com.example.onlineshoppingapp.dao.OrderDAO.ResourceNotFoundException; // Assuming you defined this inner class in OrderDAO
import com.example.onlineshoppingapp.dao.ProductDAO;
import com.example.onlineshoppingapp.dao.UserDAO;
import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.domain.Order.OrderStatus;
import com.example.onlineshoppingapp.domain.OrderItem;
import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.domain.User;
import com.example.onlineshoppingapp.dto.OrderCreationRequest;
import com.example.onlineshoppingapp.dto.OrderDetailResponse;
import com.example.onlineshoppingapp.dto.OrderItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderDAO orderDAO;
    private final UserDAO userDAO; // Needed to fetch the User object
    private final ProductDAO productDAO; // Needed to fetch the Product objects

    @Autowired
    public OrderService(OrderDAO orderDAO, UserDAO userDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
        this.productDAO = productDAO;
    }

    // --- POST: Place an order ---

    /**
     * Creates a new "Processing" order, performs inventory checks, and deducts stock.
     * @param userId The ID of the user placing the order.
     * @param request The DTO containing the list of items and quantities.
     * @return The created Order entity.
     * @throws NotEnoughInventoryException if stock is insufficient.
     * @throws ResourceNotFoundException if user or product is not found.
     */
    @Transactional
    public Order placeNewOrder(Integer userId, OrderCreationRequest request) {

        // 1. Fetch User
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // 2. Prepare Order entity
        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.PROCESSING);

        List<OrderItem> items = new ArrayList<>();

        // 3. Process DTO items, fetch products, and prepare OrderItems
        for (OrderItemRequest itemDTO : request.getItems()) {
            Integer productId = itemDTO.getProductId();
            Integer quantity = itemDTO.getQuantity();

            // Fetch Product (DAO finds the current stock and pricing)
            Product product = productDAO.findByIdForAdminView(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            // Create OrderItem, capturing prices at this moment in time
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);

            // Capture historical prices! (Essential for reporting accuracy)
            item.setRetailPrice(product.getRetailPrice());
            item.setWholesalePrice(product.getWholesalePrice());

            items.add(item);
        }

        // 4. Delegate to DAO for transaction/inventory logic
        // The DAO handles the stock check, deduction, and persistence (Order and OrderItems)
        return orderDAO.placeOrder(newOrder, items);
    }

    // --- GET methods ---

    /**
     * Retrieves all orders for a specific user.
     */
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderDAO.findAllByUserId(userId);
        List<OrderDetailResponse> list = new ArrayList<>();

        for (var o : orders) {
            list.add(getOrderDetailById(o.getId()));
        }
        return list;
    }

    /**
     * Retrieves the detail of a specific order.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetailById(Integer orderId) {
        Order order = orderDAO.findDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Map the entity to the DTO before the transaction closes
        return OrderDetailResponse.fromEntity(order);
    }

    // --- PATCH: Status Update Methods (User/Seller) ---

    /**
     * Allows a user to cancel their own order (status change from PROCESSING to CANCELED).
     */
    @Transactional
    public OrderDetailResponse cancelOrder(Integer orderId, Integer userId) {
//        // 1. Fetch the raw entity to check ownership and status
//        Order order = orderDAO.findById(orderId)
//                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
//
//        // 2. Business rule: User can only cancel their own order
//        if (!order.getUser().getId().equals(userId)) {
//            throw new SecurityException("User is not authorized to cancel this order.");
//        }

        // 3. DAO handles status update, stock increment, and persistence
        // The DAO returns the updated Order entity.
        Order updatedOrder = orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELED);

        // 4. Return the result as a DTO
        return OrderDetailResponse.fromEntity(updatedOrder);
    }

    /**
     * Allows a seller/admin to complete a processing order.
     */
    @Transactional
    public Order completeOrder(Integer orderId) {
        // DAO handles status check and persistence
        return orderDAO.updateOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    /**
     * Allows a seller/admin to cancel an order (e.g., due to local stock out).
     */
    @Transactional
    public OrderDetailResponse sellerCancelOrder(Integer orderId) {
        Order updatedOrder = orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELED);

        // 4. Return the result as a DTO
        return OrderDetailResponse.fromEntity(updatedOrder);
    }

    // --- Helper classes (Should be moved to a separate package in a real app) ---
    public static class SecurityException extends RuntimeException {
        public SecurityException(String message) {
            super(message);
        }
    }
}