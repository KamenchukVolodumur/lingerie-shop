package com.lingerieshop.paymentservice.services;

import com.lingerieshop.paymentservice.clients.MonobankClient;
import com.lingerieshop.paymentservice.clients.OrderServiceClient;
import com.lingerieshop.paymentservice.dto.*;
import com.lingerieshop.paymentservice.entities.PaymentStatus;
import com.lingerieshop.paymentservice.entities.PaymentTransaction;
import com.lingerieshop.paymentservice.exceptions.PaymentStateConflictException;
import com.lingerieshop.paymentservice.exceptions.PaymentTransactionNotFoundException;
import com.lingerieshop.paymentservice.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MonobankClient monobankClient;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MonobankWebhookSignatureVerifier signatureVerifier;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "monobankToken", "test-token");
        ReflectionTestUtils.setField(paymentService, "orderSuccessUrl", "http://localhost:5173/success");
        ReflectionTestUtils.setField(paymentService, "webHookUrl", "http://localhost:8084/webhook");
    }

    @Test
    void generatePaymentLink_createsInvoiceAndReturnsUrl() {
        PaymentRequestDTO request = new PaymentRequestDTO(1L, new BigDecimal("250.00"));
        MonoInvoiceResponseDTO monoResponse = new MonoInvoiceResponseDTO("inv_100", "https://pay.mono/100");

        when(monobankClient.createInvoice(eq("test-token"), any())).thenReturn(monoResponse);

        PaymentLinkResponseDTO response = paymentService.generatePaymentLink(request);

        assertNotNull(response);
        assertEquals("https://pay.mono/100", response.paymentUrl());
        verify(paymentRepository, times(2)).save(any(PaymentTransaction.class));
    }

    @Test
    void processWebHook_holdStatus_updatesTransactionToHold() {
        String payload = "{\"invoiceId\":\"inv_100\",\"status\":\"hold\"}";
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .status(PaymentStatus.PENDING)
                .externalBankReference("inv_100")
                .build();

        when(paymentRepository.findByExternalBankReference("inv_100")).thenReturn(Optional.of(transaction));

        paymentService.processWebHook(payload, "dummy-sig");

        assertEquals(PaymentStatus.HOLD, transaction.getStatus());
        verify(signatureVerifier).verify(payload, "dummy-sig");
    }

    @Test
    void processWebHook_failureStatus_marksOrderFailed() {
        String payload = "{\"invoiceId\":\"inv_100\",\"status\":\"failure\"}";
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .status(PaymentStatus.PENDING)
                .externalBankReference("inv_100")
                .build();

        when(paymentRepository.findByExternalBankReference("inv_100")).thenReturn(Optional.of(transaction));

        paymentService.processWebHook(payload, "dummy-sig");

        assertEquals(PaymentStatus.CANCELED, transaction.getStatus());
        verify(orderServiceClient).markPaymentFailed(10L);
    }

    @Test
    void capturePayment_heldStatus_finalizesAndSetsPaid() {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .amount(new BigDecimal("150.00"))
                .status(PaymentStatus.HOLD)
                .externalBankReference("inv_100")
                .build();

        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(transaction));
        when(monobankClient.finalizeInvoice(eq("test-token"), any()))
                .thenReturn(new MonoInvoiceFinalizeResponseDTO("success"));

        paymentService.capturePayment(10L);

        assertEquals(PaymentStatus.PAID, transaction.getStatus());
        verify(monobankClient).finalizeInvoice(eq("test-token"), any());
    }

    @Test
    void capturePayment_notHeld_throwsPaymentStateConflictException() {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .amount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(transaction));

        assertThrows(PaymentStateConflictException.class, () -> paymentService.capturePayment(10L));
    }

    @Test
    void cancelPayment_pendingStatus_removesInvoiceAndCancels() {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .status(PaymentStatus.PENDING)
                .externalBankReference("inv_100")
                .build();

        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(transaction));

        PaymentCancellationResponseDTO result = paymentService.cancelPayment(10L);

        assertEquals(PaymentStatus.CANCELED, result.paymentStatus());
        assertEquals(PaymentStatus.CANCELED, transaction.getStatus());
        verify(monobankClient).removeInvoice(eq("test-token"), any());
    }

    @Test
    void cancelPayment_alreadyPaid_throwsPaymentStateConflictException() {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id("tx-1")
                .orderId(10L)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(transaction));

        assertThrows(PaymentStateConflictException.class, () -> paymentService.cancelPayment(10L));
    }

    @Test
    void capturePayment_notFound_throwsException() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentTransactionNotFoundException.class, () -> paymentService.capturePayment(99L));
    }
}
