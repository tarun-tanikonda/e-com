package com.shopsphere.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

/**
 * Exception handler for controller-level or request input errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<GatewayErrorResponse> handleBadRequest(
            ServerWebInputException exception,
            ServerWebExchange exchange) {

        String path = exchange != null ? exchange.getRequest().getURI().getRawPath() : "";
        String correlationId = "";
        if (exchange != null) {
            correlationId = exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
            }
        }

        GatewayErrorResponse response = GatewayErrorResponse.create(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid input: " + exception.getReason(),
                path,
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
