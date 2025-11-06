package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.service.ProductService;
import com.example.onlineshoppingapp.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // --- Public View Endpoint (GET product detail) ---
    // User sees: id, name, description, retailPrice
    @JsonView(Views.PublicView.class)
    @GetMapping("/{id}")
    public Product getProductDetailPublic(@PathVariable Integer id) {
        // Assume service retrieves the product or throws exception if not found/out-of-stock
        return productService.getProductForPublicView(id);
    }

    // --- Admin View Endpoint (GET product detail - admin) ---
    // Admin sees: all fields, including wholesalePrice and quantity
    @JsonView(Views.AdminView.class)
    @GetMapping("/{id}")
    // Note: You would secure this endpoint using @PreAuthorize("hasRole('ADMIN')")
    public Product getProductDetailAdmin(@PathVariable Integer id) {
        return productService.getProductForAdminView(id);
    }

    // POST add product (Admin/Seller requirement)
    // URL: POST /api/products/admin
    /**
     * Allows the seller/admin to add a new product listing.
     * Requires the full Product entity in the request body (name, description,
     * wholesalePrice, retailPrice, and quantity).
     */
    // @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    // NOTE: This method should be secured with @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product newProduct) {
        // Validation check: Ensure the necessary fields are present before saving
        if (newProduct.getName() == null || newProduct.getRetailPrice() == null ||
                newProduct.getWholesalePrice() == null || newProduct.getQuantity() == null) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
        }

        // The service layer handles saving the product to the DAO
        Product savedProduct = productService.addNewProduct(newProduct);

        // Return 201 Created status with the saved product details
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    // --- PATCH Update Product (Admin/Seller requirement) ---
    // URL: PATCH /api/products/admin/{id}
    /**
     * Allows the seller/admin to perform a partial update on a product.
     * The request body only needs to include the fields to be changed.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Security check is crucial here
    public ResponseEntity<Product> updateProductPartially(
            @PathVariable Integer id,
            @RequestBody Product partialUpdateProduct) {

        // 1. Service handles finding the existing product and applying non-null fields
        //    from partialUpdateProduct.
        try {
            Product updatedProduct = productService.updateProductPartially(id, partialUpdateProduct);

            // 2. Return the updated resource with 200 OK or 204 No Content
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Handle cases where the product is not found
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
            }
            // Handle other server errors
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}