package com.example.onlineshoppingapp.dto;

import com.example.onlineshoppingapp.domain.Order;
import com.example.onlineshoppingapp.domain.Order.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderDetailResponse {

    private Integer id;
    private LocalDateTime orderTime;
    private OrderStatus status;
    private String placedByUsername;
    private List<OrderItemDetailResponse> items;

    // Static mapping method
    public static OrderDetailResponse fromEntity(Order order) {
        OrderDetailResponse dto = new OrderDetailResponse();
        dto.setId(order.getId());
        dto.setOrderTime(order.getOrderTime());
        dto.setStatus(order.getStatus());

        // Safety check to ensure username is available
        if (order.getUser() != null) {
            dto.setPlacedByUsername(order.getUser().getUsername());
        }

        // Map the list of OrderItem entities to OrderItemDetailResponse DTOs
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderItemDetailResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}