package com.lingerieshop.paymentservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${app.services.order-service-url}")
public interface OrderServiceClient {

    @PatchMapping("/api/v1/internal/orders/{orderId}/payment-failed")
    void markPaymentFailed(@PathVariable("orderId") Long orderId);

    @PatchMapping("/api/v1/internal/orders/{orderId}/payment-canceled")
    void markPaymentCanceled(@PathVariable("orderId") Long orderId);
}
