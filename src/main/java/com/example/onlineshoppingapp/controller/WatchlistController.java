package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.security.AuthUserDetail;
import com.example.onlineshoppingapp.service.WatchlistService;
import com.example.onlineshoppingapp.service.UserService;
import com.example.onlineshoppingapp.service.WatchlistService.ResourceNotFoundException;
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
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService; // Used for username-to-ID lookup

    @Autowired
    public WatchlistController(WatchlistService watchlistService, UserService userService) {
        this.watchlistService = watchlistService;
        this.userService = userService;
    }

    // Helper method to get the current user's ID
    private Integer getCurrentUserId(Principal principal) {
        if (principal == null) {
            // Should be caught by Spring Security, but good to have
            throw new SecurityException("Authentication principal not found.");
        }
        // Use UserService to map the username to the userId
        return userService.getUserIdByUsername(principal.getName());
    }

    // --- GET: View In-Stock Watchlist ---
    // URL: GET /api/watchlist
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Product>> getWatchlist(@AuthenticationPrincipal AuthUserDetail userDetails) {
        try {
            Integer userId = userService.getUserIdByUsername(userDetails.getUsername());
            List<Product> products = watchlistService.getInStockWatchlist(userId);

            // Note: Use @JsonView in the future to filter Product fields for the user
            return ResponseEntity.ok(products);
        } catch (UserService.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }
    }

    // --- POST: Add Product to Watchlist ---
    // URL: POST /api/watchlist/{productId}
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> addProductToWatchlist(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                      @PathVariable Integer productId) {
        try {
            Integer userId = userService.getUserIdByUsername(userDetails.getUsername());
            watchlistService.addToWatchlist(userId, productId);

            return new ResponseEntity<>(HttpStatus.CREATED); // 201 Created
        } catch (ResourceNotFoundException e) {
            // If the product ID is invalid
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }

    // --- DELETE: Remove Product from Watchlist ---
    // URL: DELETE /api/watchlist/{productId}
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteProductToWatchlist(@AuthenticationPrincipal AuthUserDetail userDetails,
                                                      @PathVariable Integer productId) {
        try {
            Integer userId = userService.getUserIdByUsername(userDetails.getUsername());
            watchlistService.removeFromWatchlist(userId, productId);

            // 204 No Content is standard for a successful DELETE
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // Log the error but often 204 is returned even if the item wasn't there (idempotence)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}