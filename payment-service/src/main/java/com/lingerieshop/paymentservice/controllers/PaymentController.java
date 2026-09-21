package com.lingerieshop.paymentservice.controllers;

import com.lingerieshop.paymentservice.dto.PaymentLinkResponseDTO;
import com.lingerieshop.paymentservice.dto.PaymentRequestDTO;
import com.lingerieshop.paymentservice.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/generate")
    public ResponseEntity<PaymentLinkResponseDTO> generatePaymentLink(
            @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.generatePaymentLink(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> processWebhook(
            @RequestHeader("X-Sign") String signature,
            @RequestBody String webhookPayload) {
        paymentService.processWebHook(webhookPayload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/capture")
    public ResponseEntity<Void> capturePayment(@PathVariable Long orderId) {
        paymentService.capturePayment(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPayment(orderId);
        return ResponseEntity.ok().build();
    }
}
