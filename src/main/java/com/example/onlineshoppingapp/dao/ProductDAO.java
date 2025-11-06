package com.example.onlineshoppingapp.dao;

import com.example.onlineshoppingapp.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persists a new Product entity to the database.
     * @param product The Product object to save.
     */
    @Transactional
    public Product save(Product product) {
        // entityManager.persist() is used for new entities
        entityManager.persist(product);
        return product;
    }

    /**
     * Finds a Product by its primary key ID.
     * @param id The ID of the product.
     * @return An Optional containing the Product, or empty if not found.
     */
    public Optional<Product> findById(Integer id) {
        // entityManager.find() retrieves an entity by its primary key
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    /**
     * Updates an existing Product entity.
     * @param product The detached Product object with updated values.
     * @return The merged (persistent) Product object.
     */
    @Transactional
    public Product update(Product product) {
        // entityManager.merge() handles updates for detached entities
        return entityManager.merge(product);
    }

    /**
     * Deletes a Product entity by its primary key ID.
     * @param id The ID of the product to delete.
     */
    @Transactional
    public void deleteById(Integer id) {
        findById(id).ifPresent(entityManager::remove);
    }

    // --- Custom Query using HQL ---

    /**
     * Retrieves all products that are currently in stock (quantity > 0).
     * This query uses HQL to fetch data for the public product list view.
     * @return A list of in-stock Product entities.
     */
    public List<Product> findAllInStock() {
        // HQL references the Java entity name (Product) and attribute name (quantity)
        String hql = "SELECT p FROM Product p WHERE p.quantity > 0";

        return entityManager.createQuery(hql, Product.class)
                .getResultList();
    }

    /**
     * Retrieves the stock quantity for a specific product.
     * @param id The ID of the product.
     * @return The stock quantity, or 0 if the product is not found.
     */
    public Integer getStockQuantity(Integer id) {
        String hql = "SELECT p.quantity FROM Product p WHERE p.id = :productId";

        try {
            return entityManager.createQuery(hql, Integer.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return 0; // Product not found
        }
    }
    // --- Product Detail Methods ---

    /**
     * Finds the FULL Product entity by ID for the Admin View.
     * The Admin is allowed to see product details regardless of stock quantity.
     * @param id The ID of the product.
     * @return An Optional containing the full Product, or empty if not found.
     */
    public Optional<Product> findByIdForAdminView(Integer id) {
        // Simple entityManager.find() is sufficient as no stock check is needed
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    /**
     * Finds the FULL Product entity by ID for the Public User View,
     * but only IF the product is in stock (quantity > 0).
     * * The filtering is handled here (DAO/Business Rule), and the presentation
     * filtering (hiding quantity) is handled by @JsonView in the Controller.
     * * @param id The ID of the product.
     * @return An Optional containing the full Product, or empty if not found OR out of stock.
     */
    public Optional<Product> findByIdForUserView(Integer id) {
        // HQL enforces the "out of stock product should NOT be shown" rule.
        String hql = "SELECT p FROM Product p WHERE p.id = :productId AND p.quantity > 0";

        try {
            Product product = entityManager.createQuery(hql, Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
            return Optional.of(product);
        } catch (NoResultException e) {
            // This catches cases where:
            // 1. The product ID doesn't exist.
            // 2. The product exists but p.quantity <= 0 (out of stock).
            return Optional.empty();
        }
    }
}