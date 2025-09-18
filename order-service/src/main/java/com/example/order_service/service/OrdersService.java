package com.example.order_service.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.order_service.repository.OrderRepository;
import com.example.order_service.model.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate;

    @Cacheable("allOrders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);
        
        OrderPlaceEvent event = OrderPlaceEvent.builder()
        .orderId(saved.getId())
        .userId(saved.getUserId())
        .price(saved.getPrice())
        .build();
        kafkaTemplate.send("orders-topic", event);
        System.out.println("Sent order to Kafka");
        return saved;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
