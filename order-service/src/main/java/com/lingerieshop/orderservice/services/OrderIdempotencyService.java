package com.lingerieshop.orderservice.services;

import com.lingerieshop.orderservice.entities.OrderIdempotencyRecord;
import com.lingerieshop.orderservice.repositories.OrderIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderIdempotencyService {

    private final OrderIdempotencyRepository orderIdempotencyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation reserve(Long userId, String idempotencyKey, String requestHash) {
        Optional<OrderIdempotencyRecord> existingRecord = orderIdempotencyRepository
                .findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (existingRecord.isPresent()) {
            OrderIdempotencyRecord record = existingRecord.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency-Key was already used with different checkout data");
            }
            if (record.getOrderId() != null) {
                return Reservation.completed(record.getOrderId(), record.getPaymentUrl());
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order request is still being processed");
        }

        OrderIdempotencyRecord record = orderIdempotencyRepository.saveAndFlush(
                new OrderIdempotencyRecord(UUID.randomUUID(), userId, idempotencyKey, requestHash));
        return Reservation.acquired(record.getId());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(UUID reservationId, Long orderId, String paymentUrl) {
        OrderIdempotencyRecord record = orderIdempotencyRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Idempotency reservation not found"));
        record.complete(orderId, paymentUrl);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(UUID reservationId) {
        orderIdempotencyRepository.deleteById(reservationId);
    }

    public record Reservation(UUID id, Long orderId, String paymentUrl) {

        private static Reservation acquired(UUID id) {
            return new Reservation(id, null, null);
        }

        private static Reservation completed(Long orderId, String paymentUrl) {
            return new Reservation(null, orderId, paymentUrl);
        }

        public boolean isCompleted() {
            return orderId != null;
        }
    }
}
