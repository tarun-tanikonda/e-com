package com.shopsphere.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterValidatorTest {

    private RouterValidator routerValidator;

    @BeforeEach
    void setUp() {
        routerValidator = new RouterValidator();
    }

    @Test
    @DisplayName("User login and registration should be public")
    void openAuthEndpointsShouldNotBeSecured() {
        MockServerHttpRequest registerRequest = MockServerHttpRequest.post("/api/v1/users/register").build();
        MockServerHttpRequest loginRequest = MockServerHttpRequest.post("/api/v1/users/login").build();

        assertFalse(routerValidator.isSecured.test(registerRequest));
        assertFalse(routerValidator.isSecured.test(loginRequest));
    }

    @Test
    @DisplayName("GET /api/v1/products should be public for catalogue browsing")
    void publicProductCatalogReadsShouldNotBeSecured() {
        MockServerHttpRequest getProducts = MockServerHttpRequest.get("/api/v1/products").build();
        MockServerHttpRequest getProductById = MockServerHttpRequest.get("/api/v1/products/42").build();

        assertFalse(routerValidator.isSecured.test(getProducts));
        assertFalse(routerValidator.isSecured.test(getProductById));
    }

    @Test
    @DisplayName("POST/PUT/DELETE on products should be secured for admin operations")
    void productMutationsShouldBeSecured() {
        MockServerHttpRequest createProduct = MockServerHttpRequest.post("/api/v1/products").build();
        MockServerHttpRequest updateProduct = MockServerHttpRequest.put("/api/v1/products/42").build();
        MockServerHttpRequest deleteProduct = MockServerHttpRequest.delete("/api/v1/products/42").build();

        assertTrue(routerValidator.isSecured.test(createProduct));
        assertTrue(routerValidator.isSecured.test(updateProduct));
        assertTrue(routerValidator.isSecured.test(deleteProduct));
    }

    @Test
    @DisplayName("Cart and Order operations must always be secured")
    void cartAndOrderEndpointsShouldBeSecured() {
        MockServerHttpRequest getCart = MockServerHttpRequest.get("/api/v1/cart").build();
        MockServerHttpRequest addToCart = MockServerHttpRequest.post("/api/v1/cart/items").build();
        MockServerHttpRequest checkout = MockServerHttpRequest.post("/api/v1/orders/checkout").build();

        assertTrue(routerValidator.isSecured.test(getCart));
        assertTrue(routerValidator.isSecured.test(addToCart));
        assertTrue(routerValidator.isSecured.test(checkout));
    }

    @Test
    @DisplayName("Actuator health and Swagger docs should be public")
    void observabilityAndDocsShouldNotBeSecured() {
        MockServerHttpRequest health = MockServerHttpRequest.get("/actuator/health").build();
        MockServerHttpRequest swagger = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        MockServerHttpRequest apiDocs = MockServerHttpRequest.get("/v3/api-docs").build();

        assertFalse(routerValidator.isSecured.test(health));
        assertFalse(routerValidator.isSecured.test(swagger));
        assertFalse(routerValidator.isSecured.test(apiDocs));
    }
}
