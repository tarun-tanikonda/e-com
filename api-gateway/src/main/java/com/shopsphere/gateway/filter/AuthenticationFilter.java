package com.shopsphere.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.gateway.exception.GatewayErrorResponse;
import com.shopsphere.gateway.security.JwtUtil;
import com.shopsphere.gateway.security.RouterValidator;

import reactor.core.publisher.Mono;

/**
 * Authentication GlobalFilter for API Gateway.
 * Intercepts requests to secured endpoints, validates Bearer JWT tokens,
 * and enriches downstream requests with authenticated user identity headers.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.routerValidator = routerValidator;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Check if the target route is public or secured
        if (!routerValidator.isSecured.test(request)) {
            return chain.filter(exchange);
        }

        // 2. Verify Authorization header is present
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", request.getURI().getRawPath());
            return onError(exchange, "Missing or malformed Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // 3. Extract and validate JWT token
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Rejected request with invalid or expired token for path: {}", request.getURI().getRawPath());
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        // 4. Extract claims and propagate identity headers downstream
        String userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String email = jwtUtil.extractEmail(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set("X-User-Id", userId);
                    h.set("X-User-Role", role);
                    h.set("X-User-Email", email);
                }))
                .build();

        log.debug("Authenticated request: userId={}, role={}, path={}", userId, role, request.getURI().getRawPath());

        return chain.filter(mutatedExchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String path = exchange.getRequest().getURI().getRawPath();
        String correlationId = response.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        }

        GatewayErrorResponse errorResponse = GatewayErrorResponse.create(httpStatus, message, path, correlationId);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            log.error("Failed to write error response", ex);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // Runs after CorrelationIdFilter (+0) and InternalRouteRestrictionFilter (+1)
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
