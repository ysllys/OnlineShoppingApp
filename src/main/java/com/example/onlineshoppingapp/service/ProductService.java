package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.ProductDAO;
import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.Views;
import com.example.onlineshoppingapp.dto.ProductCreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductDAO productDAO;

    @Autowired
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    // --- Admin/Seller Operations (POST, PATCH) ---

    /**
     * Adds a new product to the database.
     * @param newProduct The product entity to be saved.
     * @return The saved product entity.
     */
    @Transactional
    public Product addNewProduct(Product newProduct) {
        // Business logic validation (e.g., price check, minimum quantity) goes here

        // Ensure ID is null before saving (to create a new entity)
        newProduct.setId(null);
        return productDAO.save(newProduct);
    }
    public Product convertDtoToEntity(ProductCreationRequest creationDTO) {
        Product product = new Product();

        // Map data from DTO to Entity
        product.setName(creationDTO.getName());
        product.setDescription(creationDTO.getDescription());

        // Use the DTO fields to set the corresponding Entity fields
        product.setWholesalePrice(creationDTO.getWholesalePrice());
        product.setRetailPrice(creationDTO.getRetailPrice());
        product.setQuantity(creationDTO.getQuantity());

        // Default System Field Logic:
        // 1. Ensure the ID is null (important for new entity creation/persistence)
        product.setId(null);

        // 2. Set any default flags or values (if necessary)
        // product.setIsActive(true);

        // Note: Fields like 'creationDate' are often handled automatically
        // by JPA/Hibernate listeners, so you may not need to set them here.

        return product;
    }
    /**
     * Handles a partial update (PATCH) for a product by applying non-null fields
     * from the incoming payload to the existing entity.
     * @param id The ID of the product to update.
     * @param partialUpdate The Product object containing fields to change.
     * @return The updated Product entity.
     * @throws ResourceNotFoundException if the product ID does not exist.
     */
    @Transactional
    public Product updateProductPartially(Integer id, Product partialUpdate) {

        // 1. Find the existing product using the DAO's Admin view (needs all fields)
        Product existingProduct = productDAO.findByIdForAdminView(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id)); // Custom exception recommended

        // 2. Apply updates selectively (Read-Modify-Write pattern)
        // Only update fields if they are explicitly present (not null) in the incoming payload.
        if (partialUpdate.getName() != null) {
            existingProduct.setName(partialUpdate.getName());
        }
        if (partialUpdate.getDescription() != null) {
            existingProduct.setDescription(partialUpdate.getDescription());
        }
        if (partialUpdate.getWholesalePrice() != null) {
            existingProduct.setWholesalePrice(partialUpdate.getWholesalePrice());
        }
        if (partialUpdate.getRetailPrice() != null) {
            existingProduct.setRetailPrice(partialUpdate.getRetailPrice());
        }
        if (partialUpdate.getQuantity() != null) {
            existingProduct.setQuantity(partialUpdate.getQuantity());
        }

        // 3. Save the modified entity back to the database
        return productDAO.update(existingProduct);
    }

    // --- Public User/Admin Read Operations (GET) ---

    /**
     * Retrieves all in-stock products for the public product list view.
     * @return A list of in-stock Product entities.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsForPublicView() {
        // DAO enforces the stock check (quantity > 0)
        return productDAO.findAllInStock();
    }
    @Transactional(readOnly = true)
    public List<Product> getAllProductsForAdminView() {
        return productDAO.listAllProducts();
    }

    /**
     * Retrieves the product details for the Public User View.
     * Enforces the business rule: must be in stock.
     * @param id The ID of the product.
     * @return The full Product entity (filtering for public view fields is done by @JsonView in Controller).
     * @throws ResourceNotFoundException if the product is not found OR is out of stock.
     */
    @Transactional(readOnly = true)
    public Product getProductForPublicView(Integer id) {
        // DAO enforces the stock check (quantity > 0)
        return productDAO.findByIdForUserView(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or out of stock with ID: " + id));
    }

    /**
     * Retrieves the full product details for the Admin/Seller view.
     * @param id The ID of the product.
     * @return The full Product entity (all fields visible to Admin).
     * @throws ResourceNotFoundException if the product is not found.
     */
    @Transactional(readOnly = true)
    public Product getProductForAdminView(Integer id) {
        // DAO does NOT enforce stock check for admin view
        return productDAO.findByIdForAdminView(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    // --- Helper Class (Define this outside the service or as a separate file) ---

    // Define a simple runtime exception for better error handling in the Controller
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}