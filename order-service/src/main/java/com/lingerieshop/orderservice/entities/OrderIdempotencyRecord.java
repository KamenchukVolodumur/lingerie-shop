package com.lingerieshop.orderservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "order_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_idempotency_user_key",
                columnNames = {"user_id", "idempotency_key"}))
@Getter
@NoArgsConstructor
public class OrderIdempotencyRecord {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_url", length = 2_000)
    private String paymentUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrderIdempotencyRecord(UUID id, Long userId, String idempotencyKey, String requestHash) {
        this.id = id;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void complete(Long orderId, String paymentUrl) {
        this.orderId = orderId;
        this.paymentUrl = paymentUrl;
        this.updatedAt = LocalDateTime.now();
    }
}
