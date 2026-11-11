package com.shopsphere.product.service;

import java.util.List;

import com.shopsphere.product.dto.request.BatchSkuRequest;
import com.shopsphere.product.dto.request.ProductVariantRequest;
import com.shopsphere.product.dto.response.ProductVariantResponse;

public interface ProductVariantService {

	// Create a new variant for an existing product
	ProductVariantResponse createVariant(Long productId, ProductVariantRequest request);

	// Retrieve a variant by its ID
	ProductVariantResponse getVariantById(Long id);

	// Retrieve a variant by SKU (used by Cart & Order services)
	ProductVariantResponse getVariantBySku(String sku);

	// Retrieve all variants for a product (Admin view)
	List<ProductVariantResponse> getVariantsByProductId(Long productId);

	// Retrieve only active variants for a product (Shopper/Catalog view)
	List<ProductVariantResponse> getActiveVariantsByProductId(Long productId);

	// Update variant details (price, size, color, sku)
	ProductVariantResponse updateVariant(Long id, ProductVariantRequest request);

	// Enable or disable a variant
	ProductVariantResponse updateVariantStatus(Long id, Boolean active);

	// Batch lookup for Cart & Order services
	List<ProductVariantResponse> getVariantsByBatchSkus(BatchSkuRequest request);

}
