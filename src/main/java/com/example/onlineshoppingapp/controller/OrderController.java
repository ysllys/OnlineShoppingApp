package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.dao.OrderDAO.NotEnoughInventoryException;
import com.example.onlineshoppingapp.dao.OrderDAO.ResourceNotFoundException;
import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.dto.OrderCreationRequest;
import com.example.onlineshoppingapp.service.OrderService;
import com.example.onlineshoppingapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Used to get the logged-in user's info
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // --- POST: Place an order (User) ---
    // URL: POST /api/orders
    // Request Body: { "products": { "productId": quantity, ... } }
    @PostMapping
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> placeOrder(
            @Valid @RequestBody OrderCreationRequest request, // <--- Use DTO here
            Principal principal) {

        try {
            // 1. Get the username (or unique ID if configured that way)
            String username = principal.getName();

            // 2. Look up the full User object/ID using the UserService
            Integer userId = userService.getUserIdByUsername(username);

            // Pass the DTO to the service
            Order newOrder = orderService.placeNewOrder(userId, request);

            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (NotEnoughInventoryException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 Conflict
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // --- GET all orders by user ---
    // URL: GET /api/orders
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Order>> getOrdersByUser(Principal principal) {
        // 1. Get the username (or unique ID if configured that way)
        String username = principal.getName();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);

        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // --- GET order detail ---
    // URL: GET /api/orders/{id}
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> getOrderDetail(@PathVariable Integer orderId, Principal principal) {
        try {
            // 1. Get the username (or unique ID if configured that way)
            String username = principal.getName();

            // 2. Look up the full User object/ID using the UserService
            Integer userId = userService.getUserIdByUsername(username);

            // 2. **Security Lookup Required Here:** Determine isAdmin status from roles
            // This still requires accessing the full Authentication object/roles
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // 3. Call the service with the order ID, user ID, and role
            Order order = orderService.getOrderDetailById(orderId);

            if (isAdmin) ResponseEntity.ok(order);
            if (order.getUser().getId().equals(userId)) {
                return ResponseEntity.ok(order);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (OrderService.SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Access Denied
        }
    }

    // --- PATCH: Cancel Order (User) ---
    // URL: PATCH /api/orders/{id}/cancel
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> userCancelOrder(@PathVariable Integer orderId, Principal principal) {
        // 1. Get the username (or unique ID if configured that way)
        String username = principal.getName();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            Order canceledOrder;

            if (isAdmin) canceledOrder = orderService.sellerCancelOrder(orderId);
            else canceledOrder = orderService.cancelOrder(orderId, userId);

            return ResponseEntity.ok(canceledOrder); // 200 OK
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        } catch (IllegalStateException | OrderService.SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden (e.g., trying to cancel a COMPLETED order)
        }
    }

    // --- PATCH: Complete Order (Seller/Admin) ---
    // URL: PATCH /api/orders/{id}/complete
    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> completeOrder(@PathVariable Integer orderId) {
        try {
            Order completedOrder = orderService.completeOrder(orderId);
            return ResponseEntity.ok(completedOrder); // 200 OK
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden (e.g., trying to complete a CANCELED order)
        }
    }
}