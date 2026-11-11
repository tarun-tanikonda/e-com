package com.shopsphere.product.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProductVariantRequest {

	@NotBlank(message = "SKU is required")
	@Size(max = 100, message = "SKU cannot exceed 100 characters")
	private String sku;
	@Size(max = 20, message = "Size cannot exceed 20 characters")
	private String size;
	@Size(max = 50, message = "Color cannot exceed 50 characters")
	private String color;
	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than zero")
	@Digits(integer = 10, fraction = 2, message = "Price format must have up to 10 integer digits and 2 decimals")
	private BigDecimal price;
	@Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g. INR, USD)")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters (e.g. INR)")
	private String currency = "INR";

	public ProductVariantRequest() {
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	

}
