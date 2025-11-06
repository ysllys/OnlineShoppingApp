package com.example.onlineshoppingapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid // Important: validates the objects inside the list as well
    private List<OrderItemRequest> items;

}