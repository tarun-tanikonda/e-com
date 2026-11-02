package com.shopsphere.gateway.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Global CORS (Cross-Origin Resource Sharing) configuration for API Gateway.
 * Enables web and mobile frontend applications to communicate with ShopSphere services.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow all local and staging frontend origins (React, Angular, Vue, Mobile)
        corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Allowed standard REST HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allowed request headers (Authorization, Content-Type, X-Correlation-ID, etc.)
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));

        // Expose headers so frontend JavaScript can read the correlation ID and Auth tokens
        corsConfig.setExposedHeaders(Arrays.asList("X-Correlation-ID", "Authorization"));

        // Allow sending credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Preflight cache duration: 1 hour (reduces repetitive OPTIONS calls)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
