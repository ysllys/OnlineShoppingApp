package com.example.onlineshoppingapp.dao;

import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.domain.Order.OrderStatus;
import com.example.onlineshoppingapp.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class OrderItemDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Helper constant to exclude canceled orders.
     */
    private static final OrderStatus EXCLUDED_STATUS = OrderStatus.CANCELED;

    // --- User Reporting Methods ---

    /**
     * (GET frequently bought products)
     * Finds the top X most frequently purchased products for a specific user,
     * excluding canceled orders.
     * @param userId The ID of the user.
     * @param limit The maximum number of products to return (X).
     * @return List of Product entities.
     */
    public List<Product> getTopFrequentlyPurchasedProducts(Integer userId, int limit) {
        // HQL: Select the Product, group by it, count the total quantity,
        // order by total quantity DESC, then by product ID ASC (tiebreaker).
        String hql = """
            SELECT oi.product 
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.user.id = :userId 
            AND o.status != :excludedStatus
            GROUP BY oi.product.id, oi.product 
            ORDER BY SUM(oi.quantity) DESC, oi.product.id ASC 
            """; // TODO: change to quantity bought

        return entityManager.createQuery(hql, Product.class)
                .setParameter("userId", userId)
                .setParameter("excludedStatus", EXCLUDED_STATUS)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * (GET recently bought products)
     * Finds the top X most recently purchased products for a specific user,
     * excluding canceled orders.
     * @param userId The ID of the user.
     * @param limit The maximum number of products to return (X).
     * @return List of Product entities.
     */
    public List<Product> getTopRecentlyPurchasedProducts(Integer userId, int limit) {
        // HQL: Select the Product, find the MAX order time for each product,
        // order by MAX order time DESC, then by product ID ASC (tiebreaker).
        String hql = """
            SELECT oi.product 
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.user.id = :userId 
            AND o.status != :excludedStatus
            GROUP BY oi.product.id, oi.product 
            ORDER BY MAX(o.orderTime) DESC, oi.product.id ASC
            """;

        return entityManager.createQuery(hql, Product.class)
                .setParameter("userId", userId)
                .setParameter("excludedStatus", EXCLUDED_STATUS)
                .setMaxResults(limit)
                .getResultList();
    }

    // --- Seller/Admin Reporting Methods ---

    /**
     * (GET product by profit)
     * Finds the product that brought the most total profit across all non-canceled orders.
     * Profit is calculated as (Retail Price - Wholesale Price) * Quantity.
     * @return A list of Object[] containing [Product, Total Profit] (usually just the top result).
     */
    public List<Object[]> getMostProfitableProduct(int limit) {
        // HQL: Calculate profit using the historical prices stored in OrderItem (oi.retailPrice and oi.wholesalePrice).
        String hql = """
            SELECT oi.product, SUM((oi.retailPrice - oi.wholesalePrice) * oi.quantity) AS totalProfit
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status != :excludedStatus
            GROUP BY oi.product.id, oi.product 
            ORDER BY totalProfit DESC
            """;

        // We use Object[] because we are selecting two different types (Product and BigDecimal/Double).
        // Since we only need the *most* profitable, we'll limit the results to 1 in the service layer.
        return entityManager.createQuery(hql, Object[].class)
                .setParameter("excludedStatus", EXCLUDED_STATUS)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * (GET most popular products)
     * Finds the top 3 most popular (highest total quantity sold) products across all non-canceled orders.
     * @return A list of Object[] containing [Product, Total Quantity Sold].
     */
    public List<Object[]> getTopPopularProducts(int limit) {
        // HQL: Group by Product and sum the quantities, ordering by sum descending.
        String hql = """
            SELECT oi.product, SUM(oi.quantity) AS totalSold
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status != :excludedStatus
            GROUP BY oi.product.id, oi.product 
            ORDER BY totalSold DESC
            """;

        return entityManager.createQuery(hql, Object[].class)
                .setParameter("excludedStatus", EXCLUDED_STATUS)
                .setMaxResults(limit) // Limit to the top 3
                .getResultList();
    }
}