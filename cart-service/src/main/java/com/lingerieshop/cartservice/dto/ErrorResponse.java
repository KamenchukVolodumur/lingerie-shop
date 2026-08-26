package com.lingerieshop.cartservice.dto;

import java.time.Instant;

// F3-02: Structured error responses
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                Instant.now().toString(),
                status,
                error,
                message,
                path
        );
    }
}
