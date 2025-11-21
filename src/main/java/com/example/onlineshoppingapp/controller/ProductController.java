package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.Product;
import com.example.onlineshoppingapp.dto.ProductCreationRequest;
import com.example.onlineshoppingapp.dto.ProductUpdateRequest;
import com.example.onlineshoppingapp.security.AuthUserDetail;
import com.example.onlineshoppingapp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Product getProductById(@AuthenticationPrincipal AuthUserDetail userDetails, @PathVariable Integer id) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return productService.getProductForAdminView(id);
        else return productService.getProductForPublicView(id);
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public List<Product> listProducts(@AuthenticationPrincipal AuthUserDetail userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return productService.getAllProductsForAdminView();
        else return productService.getAllProductsForPublicView();
    }
    // POST add product (Admin/Seller requirement)
    // URL: POST /api/products/admin
    /**
     * Allows the seller/admin to add a new product listing.
     * Requires the full Product entity in the request body (name, description,
     * wholesalePrice, retailPrice, and quantity).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") //hasAuthority('ROLE_ADMIN')
    // NOTE: This method should be secured with @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductCreationRequest creationDTO) {

        // 1. Convert DTO to Entity (Service layer job)
        Product newProduct = productService.convertDtoToEntity(creationDTO);

        // 2. Service handles persistence
        Product savedProduct = productService.addNewProduct(newProduct);

        if (savedProduct == null) return new ResponseEntity<>(savedProduct, HttpStatus.BAD_REQUEST);
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
            @RequestBody ProductUpdateRequest request) {

        Product partialUpdate = new Product();
        if (request.getName() != null) partialUpdate.setName(request.getName());
        if (request.getDescription() != null) partialUpdate.setDescription(request.getDescription());
        if (request.getWholesalePrice() != null) partialUpdate.setWholesalePrice(request.getWholesalePrice());
        if (request.getRetailPrice() != null) partialUpdate.setRetailPrice(request.getRetailPrice());
        if (request.getQuantity() != null) partialUpdate.setQuantity(request.getQuantity());

        try {
            Product updatedProduct = productService.updateProductPartially(id, partialUpdate);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (RuntimeException e) {
            // ... error handling ...
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}