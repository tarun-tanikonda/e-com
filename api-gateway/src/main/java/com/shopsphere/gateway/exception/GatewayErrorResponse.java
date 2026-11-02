package com.shopsphere.gateway.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static GatewayErrorResponse create(
            HttpStatus status,
            String message,
            String path) {

        return new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}