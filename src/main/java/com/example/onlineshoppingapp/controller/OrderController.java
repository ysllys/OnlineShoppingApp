package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.dto.OrderCreationRequest;
import com.example.onlineshoppingapp.dto.OrderDetailResponse;
import com.example.onlineshoppingapp.exception.SecurityException;
import com.example.onlineshoppingapp.security.AuthUserDetail;
import com.example.onlineshoppingapp.service.OrderService;
import com.example.onlineshoppingapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    // Request Body: { "products": { "productId": quantity, ... } }
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal AuthUserDetail userDetails,
            @Valid @RequestBody OrderCreationRequest request) {

        String username = userDetails.getUsername();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);

        // Pass the DTO to the service
        Order newOrder = orderService.placeNewOrder(userId, request);

        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    // --- GET all orders by user ---
    // URL: GET /api/orders
    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDetailResponse>> getOrdersByUser(@AuthenticationPrincipal AuthUserDetail userDetails) {
        // 1. Get the username (or unique ID if configured that way)
        String username = userDetails.getUsername();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<OrderDetailResponse> orders;

        if (isAdmin) orders = orderService.getAllOrders();
        else orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // --- GET order detail ---
    // URL: GET /api/orders/{id}
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                              @PathVariable Integer orderId) {
        String username = userDetails.getUsername();
        // Integer userId = userService.getUserIdByUsername(username);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 1. Call the service method that returns the DTO
        OrderDetailResponse orderDto = orderService.getOrderDetailById(orderId);

        if (!isAdmin && !orderDto.getPlacedByUsername().equals(username)) {
            throw new SecurityException("Unauthorized to access this order.");
        }
        // 2. Perform Authorization Check (must be Admin OR the Order's User)
        return ResponseEntity.ok(orderDto);
    }

    // --- PATCH: Cancel Order (User) ---
    // URL: PATCH /api/orders/{id}/cancel
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDetailResponse> userCancelOrder(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                 @PathVariable Integer orderId) {
        // 1. Get the username (or unique ID if configured that way)
        String username = userDetails.getUsername();

        // 2. Look up the full User object/ID using the UserService
        Integer userId = userService.getUserIdByUsername(username);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        OrderDetailResponse canceledOrder;

        if (isAdmin) canceledOrder = orderService.sellerCancelOrder(orderId);
        else canceledOrder = orderService.cancelOrder(orderId, userId);

        return ResponseEntity.ok(canceledOrder);
    }

    // --- PATCH: Complete Order (Seller/Admin) ---
    // URL: PATCH /api/orders/{id}/complete
    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> completeOrder(@PathVariable Integer orderId) {
        OrderDetailResponse completedOrder = orderService.completeOrder(orderId);
        return ResponseEntity.ok(completedOrder);
    }
}