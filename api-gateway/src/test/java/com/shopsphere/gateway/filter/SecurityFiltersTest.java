package com.shopsphere.gateway.filter;

import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.gateway.security.JwtUtil;
import com.shopsphere.gateway.security.RouterValidator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityFiltersTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private InternalRouteRestrictionFilter internalFilter;
    private AuthenticationFilter authFilter;
    private JwtUtil jwtUtil;
    private SecretKey signingKey;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));

        RouterValidator routerValidator = new RouterValidator();
        internalFilter = new InternalRouteRestrictionFilter(objectMapper);
        authFilter = new AuthenticationFilter(routerValidator, jwtUtil, objectMapper);
    }

    private String generateToken(String userId, String role, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(signingKey)
                .compact();
    }

    @Test
    @DisplayName("Internal routes (/internal/**) should be rejected with 403 Forbidden")
    void internalRoutesShouldBeForbidden() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/internal/inventory/stock").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain mockChain = ex -> Mono.empty();

        internalFilter.filter(exchange, mockChain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Secured route without Authorization header should be rejected with 401 Unauthorized")
    void securedRouteWithoutTokenShouldBeUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/cart").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain mockChain = ex -> Mono.empty();

        authFilter.filter(exchange, mockChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Public route should pass through AuthenticationFilter without token")
    void publicRouteShouldPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/users/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainExecuted = {false};
        GatewayFilterChain mockChain = ex -> {
            chainExecuted[0] = true;
            return Mono.empty();
        };

        authFilter.filter(exchange, mockChain).block();

        assertEquals(true, chainExecuted[0]);
    }

    @Test
    @DisplayName("Secured route with valid JWT should enrich downstream headers")
    void securedRouteWithValidJwtShouldEnrichHeaders() {
        String token = generateToken("usr-789", "ROLE_ADMIN", "admin@shopsphere.com");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        final ServerWebExchange[] executedExchange = new ServerWebExchange[1];
        GatewayFilterChain mockChain = ex -> {
            executedExchange[0] = ex;
            return Mono.empty();
        };

        authFilter.filter(exchange, mockChain).block();

        assertNotNull(executedExchange[0]);
        assertEquals("usr-789", executedExchange[0].getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("ROLE_ADMIN", executedExchange[0].getRequest().getHeaders().getFirst("X-User-Role"));
        assertEquals("admin@shopsphere.com", executedExchange[0].getRequest().getHeaders().getFirst("X-User-Email"));
    }
}
