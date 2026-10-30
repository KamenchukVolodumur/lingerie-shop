package com.lingerieshop.orderservice.repositories;

import com.lingerieshop.orderservice.entities.OrderIdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotencyRecord, UUID> {

    Optional<OrderIdempotencyRecord> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
