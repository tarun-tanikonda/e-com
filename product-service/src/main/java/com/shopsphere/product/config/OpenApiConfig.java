package com.shopsphere.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI productServiceOpenAPI() {
		return new OpenAPI().info(new Info().title("ShopSphere - Product Service API").description(
				"Product Catalogue, Category, and Variant Pricing Microservice for the ShopSphere E-commerce Platform.")
				.version("v1.0.0").contact(new Contact().name("ShopSphere Backend Team").email("dev@shopsphere.com"))
				.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
	}

}
