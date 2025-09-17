package com.example.order_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.order_service.UserClient;
import com.example.order_service.UserDTO;
import com.example.order_service.OrderResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")

public class OrderController {

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            Long userId = (Long) authentication.getPrincipal();
            order.setUserId(userId);
        }
        return orderRepository.save(order);
    }
    

    @GetMapping
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) 
    {       
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        UserDTO user = null;
        if (order.getUserId() != null) {
            try {
                user = userClient.getUserById(order.getUserId());
            } catch (Exception ex) {
                // Swallow user fetch errors to avoid 500; still return order info
                user = null;
            }
        }
        return new OrderResponse(order.getId(), order.getProduct(), order.getPrice(), user);        
    }
   
}
