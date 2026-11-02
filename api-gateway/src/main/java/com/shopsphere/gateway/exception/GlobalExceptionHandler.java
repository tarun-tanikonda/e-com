package com.shopsphere.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<GatewayErrorResponse> handleBadRequest(
            ServerWebInputException exception) {

        GatewayErrorResponse response = GatewayErrorResponse.create(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid input",
                ""
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}