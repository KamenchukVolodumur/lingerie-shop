package com.lingerieshop.orderservice.services;

import com.lingerieshop.orderservice.entities.OrderIdempotencyRecord;
import com.lingerieshop.orderservice.repositories.OrderIdempotencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderIdempotencyServiceTest {

    @Mock
    private OrderIdempotencyRepository orderIdempotencyRepository;

    @InjectMocks
    private OrderIdempotencyService orderIdempotencyService;

    @Test
    void reserve_newKey_acquiresReservation() {
        Long userId = 1L;
        String key = "key-123";
        String hash = "hash-abc";

        when(orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, key))
                .thenReturn(Optional.empty());
        when(orderIdempotencyRepository.saveAndFlush(any(OrderIdempotencyRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderIdempotencyService.Reservation reservation = orderIdempotencyService.reserve(userId, key, hash);

        assertNotNull(reservation);
        assertNotNull(reservation.id());
        assertFalse(reservation.isCompleted());
        verify(orderIdempotencyRepository).saveAndFlush(any(OrderIdempotencyRecord.class));
    }

    @Test
    void reserve_existingCompleted_returnsCompletedReservation() {
        Long userId = 1L;
        String key = "key-123";
        String hash = "hash-abc";

        OrderIdempotencyRecord existing = new OrderIdempotencyRecord(UUID.randomUUID(), userId, key, hash);
        existing.complete(42L, "https://pay.monobank.ua/123");

        when(orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, key))
                .thenReturn(Optional.of(existing));

        OrderIdempotencyService.Reservation reservation = orderIdempotencyService.reserve(userId, key, hash);

        assertNotNull(reservation);
        assertTrue(reservation.isCompleted());
        assertEquals(42L, reservation.orderId());
        assertEquals("https://pay.monobank.ua/123", reservation.paymentUrl());
        verify(orderIdempotencyRepository, never()).saveAndFlush(any());
    }

    @Test
    void reserve_sameKeyDifferentHash_throwsConflict() {
        Long userId = 1L;
        String key = "key-123";

        OrderIdempotencyRecord existing = new OrderIdempotencyRecord(UUID.randomUUID(), userId, key, "hash-original");
        when(orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, key))
                .thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                orderIdempotencyService.reserve(userId, key, "hash-different")
        );

        assertTrue(ex.getReason().contains("Idempotency-Key was already used with different checkout data"));
    }

    @Test
    void reserve_sameKeyStillProcessing_throwsConflict() {
        Long userId = 1L;
        String key = "key-123";
        String hash = "hash-abc";

        OrderIdempotencyRecord inProgress = new OrderIdempotencyRecord(UUID.randomUUID(), userId, key, hash);
        when(orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, key))
                .thenReturn(Optional.of(inProgress));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                orderIdempotencyService.reserve(userId, key, hash)
        );

        assertTrue(ex.getReason().contains("This order request is still being processed"));
    }

    @Test
    void complete_validReservation_updatesRecord() {
        UUID resId = UUID.randomUUID();
        OrderIdempotencyRecord record = new OrderIdempotencyRecord(resId, 1L, "key-1", "hash-1");

        when(orderIdempotencyRepository.findById(resId)).thenReturn(Optional.of(record));

        orderIdempotencyService.complete(resId, 99L, "https://pay.monobank.ua/99");

        assertEquals(99L, record.getOrderId());
        assertEquals("https://pay.monobank.ua/99", record.getPaymentUrl());
    }

    @Test
    void complete_notFound_throwsIllegalStateException() {
        UUID resId = UUID.randomUUID();
        when(orderIdempotencyRepository.findById(resId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                orderIdempotencyService.complete(resId, 99L, "https://pay.monobank.ua/99")
        );
    }

    @Test
    void release_deletesReservationById() {
        UUID resId = UUID.randomUUID();

        orderIdempotencyService.release(resId);

        verify(orderIdempotencyRepository).deleteById(resId);
    }
}
