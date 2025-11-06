package com.example.onlineshoppingapp.dao;

import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.domain.User;
import com.example.onlineshoppingapp.domain.Watchlist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WatchlistDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves all IN-STOCK Products that are currently on a specific user's watchlist.
     * @param userId The ID of the user.
     * @return A list of in-stock Product entities.
     */
    public List<Product> getInStockWatchlistProducts(Integer userId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);

        Root<Watchlist> watchlistRoot = cq.from(Watchlist.class);

        // 1. Define Joins
        // Join from Watchlist to Product
        Join<Watchlist, Product> productJoin = watchlistRoot.join("product", JoinType.INNER);

        // Join from Watchlist to User (to filter by user ID)
        Join<Watchlist, User> userJoin = watchlistRoot.join("user", JoinType.INNER);

        // 2. Define Predicates (WHERE clauses)
        // a) Filter by User ID (using the user relationship)
        Predicate userPredicate = cb.equal(
                userJoin.get("id"),
                userId
        );

        // b) Filter by Stock Quantity (quantity > 0)
        Predicate stockPredicate = cb.greaterThan(
                productJoin.get("quantity"),
                0
        );

        // 3. Combine Predicates and Select
        // Select the Product entities from the Product Join
        cq.select(productJoin)
                .where(cb.and(userPredicate, stockPredicate))
                .distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Removes a product from a user's watchlist.
     * NOTE: This method requires fetching the specific Watchlist entry first.
     */
    public void removeProductFromWatchlist(Integer userId, Integer productId) {
        // A more efficient way would be to use a JPQL DELETE query, but we use Criteria API principles:

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Watchlist> cq = cb.createQuery(Watchlist.class);
        Root<Watchlist> root = cq.from(Watchlist.class);

        // Join to access the user and product IDs for filtering
        Join<Watchlist, User> userJoin = root.join("user");
        Join<Watchlist, Product> productJoin = root.join("product");

        cq.where(
                cb.equal(userJoin.get("id"), userId),
                cb.equal(productJoin.get("id"), productId)
        );

        // Execute a find to get the entity
        Watchlist watchlist = entityManager.createQuery(cq).getSingleResult();

        if (watchlist != null) {
            entityManager.remove(watchlist);
        }
    }

    /**
     * Adds a product to a user's watchlist.
     */
    public void addProductToWatchlist(User user, Product product) {
        Watchlist watchlist = new Watchlist(user, product);
        entityManager.persist(watchlist);
    }
}