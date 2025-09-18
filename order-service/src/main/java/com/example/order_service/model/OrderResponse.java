package com.example.order_service.model;

import com.example.order_service.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class OrderResponse {
    private Long orderId;
    private String product;
    private Double price;
    private UserDTO user;
}
