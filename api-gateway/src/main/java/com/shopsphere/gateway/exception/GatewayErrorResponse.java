package com.shopsphere.gateway.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;

/**
 * Standard error response DTO for API Gateway.
 * Follows ShopSphere project guidelines: standard Java class with private fields,
 * getters, setters, constructors, and correlation tracking.
 */
public class GatewayErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String correlationId;

    public GatewayErrorResponse() {
        this.timestamp = Instant.now();
    }

    public GatewayErrorResponse(Instant timestamp, int status, String error, String message, String path, String correlationId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
    }

    public static GatewayErrorResponse create(HttpStatus status, String message, String path, String correlationId) {
        return new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                correlationId
        );
    }

    // Getters and Setters
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
