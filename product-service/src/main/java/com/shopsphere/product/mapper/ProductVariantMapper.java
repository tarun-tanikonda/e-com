package com.shopsphere.product.mapper;

import org.springframework.stereotype.Component;

import com.shopsphere.product.dto.request.ProductVariantRequest;
import com.shopsphere.product.dto.response.ProductVariantResponse;
import com.shopsphere.product.entity.ProductVariant;

@Component
public class ProductVariantMapper {

	// Convert Request DTO to Entity
	public ProductVariant toEntity(ProductVariantRequest request) {
		ProductVariant variant = new ProductVariant();
		variant.setSku(request.getSku().trim().toUpperCase());
		variant.setSize(request.getSize() != null ? request.getSize().trim() : null);
		variant.setColor(request.getColor() != null ? request.getColor().trim() : null);
		variant.setPrice(request.getPrice());
		variant.setCurrency(request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : "INR");
		variant.setActive(true);
		return variant;
	}

	// Convert Entity to Response DTO
	public ProductVariantResponse toResponse(ProductVariant variant) {
		ProductVariantResponse response = new ProductVariantResponse();
		response.setId(variant.getId());
		if (variant.getProduct() != null) {
			response.setProductId(variant.getProduct().getId());
			response.setProductName(variant.getProduct().getName());
		}
		response.setSku(variant.getSku());
		response.setSize(variant.getSize());
		response.setColor(variant.getColor());
		response.setPrice(variant.getPrice());
		response.setCurrency(variant.getCurrency());
		response.setActive(variant.getActive());
		response.setCreatedAt(variant.getCreatedAt());
		response.setUpdatedAt(variant.getUpdatedAt());
		return response;
	}

}
