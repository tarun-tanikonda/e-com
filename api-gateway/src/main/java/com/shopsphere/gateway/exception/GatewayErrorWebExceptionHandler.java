package com.shopsphere.gateway.exception;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange,Throwable exception) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred";

        String path = exchange.getRequest()
                .getURI()
                .getRawPath();

        // Gateway route not found
        if (exception.getMessage() != null && exception.getMessage().contains("404")) {

            status = HttpStatus.NOT_FOUND;
            message = "No route found for the requested path";
        }else if (isServiceUnavailable(exception)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "The requested service is temporarily unavailable";
        } else {
            return Mono.error(exception);
        }

        GatewayErrorResponse response = GatewayErrorResponse.create(status, message, path);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);
            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(bytes);

            return exchange.getResponse().writeWith(Mono.just(buffer));

        } catch (Exception serializationException) {
            byte[] fallback = (
                    "{\"status\":500,\"error\":\"Internal Server Error\","
                    + "\"message\":\"An unexpected error occurred\"}"
            ).getBytes(StandardCharsets.UTF_8);

            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(fallback);

            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
    
    private boolean isServiceUnavailable(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof java.net.ConnectException || current instanceof java.net.UnknownHostException
                    || current instanceof java.util.concurrent.TimeoutException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}