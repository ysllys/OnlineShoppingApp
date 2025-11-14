package com.example.onlineshoppingapp.dto;

import com.example.onlineshoppingapp.domain.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDetailResponse {

    private String productName;
    private Double retailPriceAtOrder;
    private Integer quantity;

    // Static mapping method
    public static OrderItemDetailResponse fromEntity(OrderItem item) {
        OrderItemDetailResponse dto = new OrderItemDetailResponse();

        // Safely access product name and price
        if (item.getProduct() != null) {
            dto.setProductName(item.getProduct().getName());
            dto.setRetailPriceAtOrder(item.getProduct().getRetailPrice());
        }
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}