package com.example.onlineshoppingapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreationRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description; // Optional

    @NotNull(message = "Wholesale price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double wholesalePrice;

    @NotNull(message = "Retail price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double retailPrice;

    @NotNull(message = "Initial quantity is required")
    @Min(value = 1, message = "Initial stock must be at least 1")
    private Integer quantity;
}