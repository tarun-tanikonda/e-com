package com.shopsphere.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String requestCorrelationId = correlationId;

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers ->
                        headers.set(CORRELATION_ID, requestCorrelationId)))
                .build();

        exchange.getResponse().getHeaders().set(CORRELATION_ID, requestCorrelationId);

        long startTime = System.currentTimeMillis();

        log.info(
                "Incoming request: method={}, path={}, correlationId={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getRawPath(),
                requestCorrelationId
        );

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;

                    log.info(
                            "Request completed: method={}, path={}, status={}, correlationId={}, durationMs={}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getRawPath(),
                            exchange.getResponse().getStatusCode(),
                            requestCorrelationId,
                            duration
                    );
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}