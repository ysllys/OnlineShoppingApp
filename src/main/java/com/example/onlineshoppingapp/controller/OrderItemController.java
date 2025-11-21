package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.security.AuthUserDetail;
import com.example.onlineshoppingapp.service.OrderItemService;
import com.example.onlineshoppingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products") // Using a dedicated path for analytics/reporting
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final UserService userService; // Used for username-to-ID lookup

    @Autowired
    public OrderItemController(OrderItemService orderItemService, UserService userService) {
        this.orderItemService = orderItemService;
        this.userService = userService;
    }

    // Helper method to get the current user's ID

    // --- User Reports ---

    /**
     * (GET frequently bought products)
     * URL: GET /api/reports/user/frequent?limit=3
     * Secured for the authenticated user.
     */
    @GetMapping("/frequent/{limit}")
    @PreAuthorize("hasRole('USER')")
    // Note: The @JsonView PublicView should be applied to the List<Product> return type
    public ResponseEntity<List<Map<String, Object>>> getTopFrequentlyBoughtProducts(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                                        @PathVariable int limit) {
        try {
            Integer userId = userService.getUserIdByUsername(userDetails.getUsername());
            List<Map<String, Object>> products = orderItemService.getTopFrequentlyPurchasedProducts(userId, limit);
            return ResponseEntity.ok(products);
        } catch (UserService.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * (GET recently bought products)
     * URL: GET /api/reports/user/recent?limit=3
     * Secured for the authenticated user.
     */
    @GetMapping("/recent/{limit}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Product>> getTopRecentlyBoughtProducts(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                                      @PathVariable int limit) {
        try {
            Integer userId = userService.getUserIdByUsername(userDetails.getUsername());
            List<Product> products = orderItemService.getTopRecentlyPurchasedProducts(userId, limit);
            return ResponseEntity.ok(products);
        } catch (UserService.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // --- Seller/Admin Reports ---

    /**
     * (GET which product brings the most profit)
     * URL: GET /api/reports/admin/most-profit
     * Secured for ADMIN role.
     */
    @GetMapping("/profit/{limit}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getMostProfitableProduct(@PathVariable int limit) {
        List<Map<String, Object>> result = orderItemService.getMostProfitableProduct(limit);
        return ResponseEntity.ok(result);
    }

    /**
     * (GET which 3 products are the most popular/sold)
     * URL: GET /api/reports/admin/most-popular
     * Secured for ADMIN role.
     */
    @GetMapping("/popular/{limit}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTopPopularProducts(@PathVariable int limit) {
        List<Map<String, Object>> result = orderItemService.getTopPopularProducts(limit);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/sold/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> getTopPopularProducts() {
        Integer result = orderItemService.getTotalProductSoldCount();
        return ResponseEntity.ok(result);
    }
    // --- Custom Security Exception ---
    private static class SecurityException extends RuntimeException {
        public SecurityException(String message) {
            super(message);
        }
    }
}