package com.lingerieshop.paymentservice.clients;

import com.lingerieshop.paymentservice.dto.MonoInvoiceRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceResponseDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceFinalizeRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceFinalizeResponseDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceRemoveRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoPublicKeyResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/api/merchant/invoice/finalize")
    MonoInvoiceFinalizeResponseDTO finalizeInvoice(
            @RequestHeader("X-Token") String token,
            @RequestBody MonoInvoiceFinalizeRequestDTO request
    );

    @PostMapping("/api/merchant/invoice/remove")
    void removeInvoice(
            @RequestHeader("X-Token") String token,
            @RequestBody MonoInvoiceRemoveRequestDTO request
    );

    @GetMapping("/api/merchant/pubkey")
    MonoPublicKeyResponseDTO getPublicKey(@RequestHeader("X-Token") String token);
}
