package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.WatchlistDAO;
import com.example.onlineshoppingapp.dao.ProductDAO;
import com.example.onlineshoppingapp.dao.UserDAO;
import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    private final WatchlistDAO watchlistDAO;
    private final ProductDAO productDAO;
    private final UserService userService; // Using UserService for user lookup

    @Autowired
    public WatchlistService(WatchlistDAO watchlistDAO, ProductDAO productDAO, UserService userService) {
        this.watchlistDAO = watchlistDAO;
        this.productDAO = productDAO;
        this.userService = userService;
    }

    // --- GET: View Watchlist ---

    /**
     * Retrieves all products on a user's watchlist that are currently in stock.
     * The DAO handles the filtering (quantity > 0).
     * @param userId The ID of the user whose watchlist is being viewed.
     * @return A list of in-stock Product entities.
     */
    @Transactional(readOnly = true)
    public List<Product> getInStockWatchlist(Integer userId) {
        return watchlistDAO.getInStockWatchlistProducts(userId);
    }

    // --- POST: Add to Watchlist ---

    /**
     * Adds a specific product to a user's watchlist.
     * @param userId The ID of the user.
     * @param productId The ID of the product to add.
     * @throws ResourceNotFoundException if the user or product does not exist.
     */
    @Transactional
    public void addToWatchlist(Integer userId, Integer productId) {
        // 1. Fetch required entities
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Use Admin view as we just need the entity, stock doesn't matter for adding
        Product product = productDAO.findByIdForAdminView(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // 2. Delegate to DAO
        // The DAO handles the persistence and the creation of the Watchlist entity
        watchlistDAO.addProductToWatchlist(user, product);
    }

    // --- DELETE: Remove from Watchlist ---

    /**
     * Removes a product from a user's watchlist.
     * @param userId The ID of the user.
     * @param productId The ID of the product to remove.
     */
    @Transactional
    public void removeFromWatchlist(Integer userId, Integer productId) {
        // Validation (optional but recommended): Check if user/product exist before deleting
        if (userService.findById(userId).isEmpty() || productDAO.findByIdForAdminView(productId).isEmpty()) {
            // In a DELETE operation, sometimes we just allow it to fail silently if the record doesn't exist.
            // Here we can throw a 404 if the user or product themselves don't exist, or just proceed.
        }

        // Delegate to DAO
        // The DAO handles locating the WatchlistKey and performing the deletion
        watchlistDAO.removeProductFromWatchlist(userId, productId);
    }

    // --- Helper Class (Assuming this is defined in UserService or common package) ---
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}