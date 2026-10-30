package com.lingerieshop.paymentservice.controllers;

import com.lingerieshop.paymentservice.dto.ApiErrorResponseDTO;
import com.lingerieshop.paymentservice.exceptions.MonobankOperationException;
import com.lingerieshop.paymentservice.exceptions.PaymentStateConflictException;
import com.lingerieshop.paymentservice.exceptions.PaymentTransactionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentTransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePaymentNotFound(
            PaymentTransactionNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(PaymentStateConflictException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePaymentStateConflict(
            PaymentStateConflictException exception) {
        return error(HttpStatus.CONFLICT, "PAYMENT_STATE_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(MonobankOperationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMonobankFailure(
            MonobankOperationException exception) {
        log.error("Monobank payment operation failed", exception);
        return error(HttpStatus.BAD_GATEWAY, "MONOBANK_OPERATION_FAILED", exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponseDTO> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponseDTO(code, message));
    }
}
