package com.lingerieshop.orderservice.controllers;

import com.lingerieshop.orderservice.dto.CreateOrderRequestDTO;
import com.lingerieshop.orderservice.dto.OrderResponseDTO;
import com.lingerieshop.orderservice.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // GET /api/v1/orders/user/5?page=0&size=10&sort=createdAt,desc
    @GetMapping("/user/{userId}")
    public Page<OrderResponseDTO> getUserOrders(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return orderService.getUserOrders(userId, pageable);
    }

    @DeleteMapping("/{id}/cancel")
    public OrderResponseDTO cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
