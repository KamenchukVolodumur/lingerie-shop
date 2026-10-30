package com.lingerieshop.orderservice.controllers;

import com.lingerieshop.orderservice.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderId}/payment-failed")
    public ResponseEntity<Void> markPaymentFailed(@PathVariable Long orderId) {
        orderService.markPaymentFailed(orderId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/payment-canceled")
    public ResponseEntity<Void> markPaymentCanceled(@PathVariable Long orderId) {
        orderService.markPaymentCanceled(orderId);
        return ResponseEntity.ok().build();
    }
}
