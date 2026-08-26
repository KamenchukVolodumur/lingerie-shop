package com.lingerieshop.orderservice.repositories;

import com.lingerieshop.orderservice.entities.Order;
import com.lingerieshop.orderservice.entities.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findAll(Pageable pageable);
}
