package com.example.onlineshoppingapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

    @NotNull
    private Integer productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}