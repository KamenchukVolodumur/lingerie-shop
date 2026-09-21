package com.lingerieshop.orderservice.clients;

import com.lingerieshop.orderservice.dto.PaymentLinkResponseDTO;
import com.lingerieshop.orderservice.dto.PaymentRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${app.services.payment-service-url}")
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/generate")
    PaymentLinkResponseDTO generatePaymentLink(@RequestBody PaymentRequestDTO request);

    @PostMapping("/api/v1/payments/{orderId}/capture")
    void capturePayment(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/v1/payments/{orderId}/cancel")
    void cancelPayment(@PathVariable("orderId") Long orderId);
}
