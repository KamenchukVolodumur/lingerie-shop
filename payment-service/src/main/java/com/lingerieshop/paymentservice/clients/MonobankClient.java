package com.lingerieshop.paymentservice.clients;

import com.lingerieshop.paymentservice.dto.MonoInvoiceRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "monobank-client", url = "${app.monobank.api-url}")
public interface MonobankClient {
    @PostMapping("/api/merchant/invoice/create")
    MonoInvoiceResponseDTO createInvoice(
            @RequestHeader("X-Token") String token,
            @RequestBody MonoInvoiceRequestDTO request
    );
}
