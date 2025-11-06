package com.example.onlineshoppingapp.dto;

// NOTE: All fields are Optional or wrappers to detect if they were present in the payload.

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {

    private String name;
    private String description;
    private Double wholesalePrice; // Using wrapper Double to allow null/omitted distinction
    private Double retailPrice;
    private Integer quantity;
}