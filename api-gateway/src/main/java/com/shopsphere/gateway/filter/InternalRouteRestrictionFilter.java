package com.shopsphere.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.gateway.exception.GatewayErrorResponse;

import reactor.core.publisher.Mono;

/**
 * Security GlobalFilter that blocks any external client attempt to access
 * internal-only microservice routes (e.g. /internal/**) with HTTP 403 Forbidden.
 */
@Component
public class InternalRouteRestrictionFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(InternalRouteRestrictionFilter.class);
    private static final String INTERNAL_PATH_PREFIX = "/internal";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final ObjectMapper objectMapper;

    public InternalRouteRestrictionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();

        if (path.startsWith(INTERNAL_PATH_PREFIX)) {
            log.warn("Blocked external attempt to access restricted internal path: {}", path);
            return onForbidden(exchange, "Direct access to internal endpoints is forbidden", HttpStatus.FORBIDDEN);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onForbidden(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
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
            log.error("Failed to write forbidden error response", ex);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // Runs right after CorrelationIdFilter and before AuthenticationFilter
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
