package com.shopsphere.gateway.security;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Route validator to determine whether an incoming HTTP request is public
 * or requires Bearer JWT authentication.
 */
@Component
public class RouterValidator {

    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/users/register",
            "/api/v1/users/login",
            "/api/v1/users/auth",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui"
    );

    /**
     * Predicate returning true if the route requires authentication.
     */
    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getRawPath();
        HttpMethod method = request.getMethod();

        // Public read access for product catalog browsing
        if (path.startsWith("/api/v1/products") && HttpMethod.GET.equals(method)) {
            return false;
        }

        // Allow explicitly open endpoints
        return OPEN_API_ENDPOINTS.stream()
                .noneMatch(path::startsWith);
    };
}
