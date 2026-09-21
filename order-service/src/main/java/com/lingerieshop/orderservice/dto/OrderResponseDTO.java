package com.lingerieshop.orderservice.dto;

import com.lingerieshop.orderservice.entities.enums.OrderStatus;
import com.lingerieshop.orderservice.entities.enums.PaymentMethod;
import com.lingerieshop.orderservice.entities.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String orderNumber,
        Long userId,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        BigDecimal shippingCost,
        String shippingAddress,
        String shippingPhone,
        List<OrderItemResponseDTO> items,
        LocalDateTime confirmationEmailSentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String paymentUrl
) {}
