package com.lingerieshop.orderservice.controllers;

import com.lingerieshop.orderservice.dto.OrderResponseDTO;
import com.lingerieshop.orderservice.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@AllArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // Список всіх замовлень
    // GET /api/v1/admin/orders?page=0&size=20&sort=createdAt,desc
    @GetMapping
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // Підтвердити замовлення: NEW -> CONFIRMED
    @PatchMapping("/{id}/confirm")
    public OrderResponseDTO confirmOrder(@PathVariable Long id) {
        return orderService.confirmOrder(id);
    }

    // Позначити що лист підтвердження відправлено
    @PatchMapping("/{id}/email-sent")
    public OrderResponseDTO markConfirmationEmailSent(@PathVariable Long id) {
        return orderService.markConfirmationEmailSent(id);
    }

    // Позначити як оплачено: PENDING -> PAID
    @PatchMapping("/{id}/pay")
    public OrderResponseDTO markAsPaid(@PathVariable Long id) {
        return orderService.markAsPaid(id);
    }

    // Позначити як відправлено на пошту: CONFIRMED -> SHIPPED
    @PatchMapping("/{id}/ship")
    public OrderResponseDTO markAsShipped(@PathVariable Long id) {
        return orderService.markAsShipped(id);
    }

    // Позначити як завершено: SHIPPED -> COMPLETED
    @PatchMapping("/{id}/complete")
    public OrderResponseDTO completeOrder(@PathVariable Long id) {
        return orderService.completeOrder(id);
    }

    // Скасувати замовлення
    @PatchMapping("/{id}/cancel")
    public OrderResponseDTO cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
