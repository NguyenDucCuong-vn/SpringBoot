package com.example.order_service.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlaceEvent {
    private Long orderId;
    private Long userId;
    private Double price;
}
