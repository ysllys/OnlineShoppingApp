package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.OrderItemDAO;
import com.example.onlineshoppingapp.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemService {

    private final OrderItemDAO orderItemDAO;
    private final UserService userService; // For validation/lookup

    @Autowired
    public OrderItemService(OrderItemDAO orderItemDAO, UserService userService) {
        this.orderItemDAO = orderItemDAO;
        this.userService = userService;
    }

    // --- User Reporting Methods ---

    /**
     * Retrieves the top X most frequently purchased products for a user.
     * @param userId The ID of the user.
     * @param limit The number of items to return.
     * @return List of Product entities.
     */
    @Transactional(readOnly = true)
    public List<Product> getTopFrequentlyPurchasedProducts(Integer userId, int limit) {
        // Validate user existence (optional, but good practice)
        userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return orderItemDAO.getTopFrequentlyPurchasedProducts(userId, limit);
    }

    /**
     * Retrieves the top X most recently purchased products for a user.
     * @param userId The ID of the user.
     * @param limit The number of items to return.
     * @return List of Product entities.
     */
    @Transactional(readOnly = true)
    public List<Product> getTopRecentlyPurchasedProducts(Integer userId, int limit) {
        userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return orderItemDAO.getTopRecentlyPurchasedProducts(userId, limit);
    }


    // --- Seller/Admin Reporting Methods ---

    /**
     * Retrieves the top 3 most popular (sold quantity) products globally.
     * @return List of Map entries containing Product and total quantity sold.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopPopularProducts(int limit) {
        // DAO returns List<Object[]>, we map it to List<Map> for cleaner JSON output
        List<Object[]> results = orderItemDAO.getTopPopularProducts(limit);

        return results.stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("product", (Product) obj[0]);
                    map.put("totalSold", (Long) obj[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the product that has generated the most total profit.
     * @return Map containing the Product and the total profit generated.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMostProfitableProduct(int limit) {
        // DAO returns List<Object[]> (limited to 1 result)
        List<Object[]> results = orderItemDAO.getMostProfitableProduct(limit);

        if (results.isEmpty()) {
            return Map.of("message", "No completed orders found to calculate profit.");
        }

        Object[] result = results.get(0);

        Map<String, Object> map = new HashMap<>();
        map.put("product", (Product) result[0]);
        map.put("totalProfit", (Double) result[1]); // Assuming the SUM aggregate returns a Double/BigDecimal

        return map;
    }

    // --- Helper Class (Defined in UserService, but included for completeness) ---
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}