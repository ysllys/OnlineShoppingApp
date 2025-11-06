package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.dao.OrderDAO.NotEnoughInventoryException;
import com.example.onlineshoppingapp.dao.OrderDAO.ResourceNotFoundException;
import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.service.OrderService;
import com.example.onlineshoppingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Used to get the logged-in user's info
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Order> placeOrder(@RequestBody Map<String, Map<Integer, Integer>> requestBody, Principal principal) {
        // In a real app, principal.getName() would be used to find the User entity ID
        // 1. Get the username (or unique ID if configured that way)
        String username = principal.getName();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);

        // Extract the map of product IDs and quantities
        Map<Integer, Integer> productQuantities = requestBody.get("products");

        if (productQuantities == null || productQuantities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Order newOrder = orderService.placeNewOrder(userId, productQuantities);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED); // 201
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

        try {
            Order canceledOrder = orderService.cancelOrder(orderId, userId);
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

    // --- PATCH: Cancel Order (Seller/Admin for sold out locally) ---
    // URL: PATCH /api/orders/{id}/admin-cancel
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> adminCancelOrder(@PathVariable Integer orderId) {
        try {
            Order canceledOrder = orderService.sellerCancelOrder(orderId);
            return ResponseEntity.ok(canceledOrder); // 200 OK
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }
    }
}