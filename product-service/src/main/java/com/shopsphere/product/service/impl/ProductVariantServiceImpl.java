package com.shopsphere.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.product.dto.request.BatchSkuRequest;
import com.shopsphere.product.dto.request.ProductVariantRequest;
import com.shopsphere.product.dto.response.ProductVariantResponse;
import com.shopsphere.product.entity.Product;
import com.shopsphere.product.entity.ProductStatus;
import com.shopsphere.product.entity.ProductVariant;
import com.shopsphere.product.mapper.ProductVariantMapper;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.product.repository.ProductVariantRepository;
import com.shopsphere.product.service.ProductVariantService;

@Service
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {

	private final ProductVariantRepository productVariantRepository;
	private final ProductRepository productRepository;
	private final ProductVariantMapper productVariantMapper;

	public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository,
			ProductRepository productRepository, ProductVariantMapper productVariantMapper) {
		this.productVariantRepository = productVariantRepository;
		this.productRepository = productRepository;
		this.productVariantMapper = productVariantMapper;
	}

	@Override
	public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
		// 1. Verify parent product exists
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
		// 2. Business rule: Cannot add variants to an archived product
		if (product.getStatus() == ProductStatus.ARCHIVED) {
			throw new IllegalArgumentException("Cannot add variants to an archived product");
		}
		String normalizedSku = request.getSku().trim().toUpperCase();
		// 3. Uniqueness rule: SKU must be globally unique
		if (productVariantRepository.findBySkuIgnoreCase(normalizedSku).isPresent()) {
			throw new IllegalArgumentException("Variant with SKU '" + normalizedSku + "' already exists");
		}
		// 4. Constraint rule: Unique (productId, size, color) combination
		String size = request.getSize() != null ? request.getSize().trim() : null;
		String color = request.getColor() != null ? request.getColor().trim() : null;
		if (productVariantRepository.existsByProductIdAndSizeAndColor(productId, size, color)) {
			throw new IllegalArgumentException(
					"A variant with size '" + size + "' and color '" + color + "' already exists for this product");
		}
		// 5. Map DTO to entity and associate with parent product
		ProductVariant variant = productVariantMapper.toEntity(request);
		variant.setProduct(product);
		ProductVariant savedVariant = productVariantRepository.save(variant);
		return productVariantMapper.toResponse(savedVariant);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductVariantResponse getVariantById(Long id) {
		ProductVariant variant = getVariantEntityById(id);
		return productVariantMapper.toResponse(variant);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductVariantResponse getVariantBySku(String sku) {
		if (sku == null || sku.trim().isEmpty()) {
			throw new IllegalArgumentException("SKU cannot be empty");
		}
		ProductVariant variant = productVariantRepository.findBySkuIgnoreCase(sku.trim()).orElseThrow(
				() -> new IllegalArgumentException("Variant not found with SKU: " + sku.trim().toUpperCase()));
		return productVariantMapper.toResponse(variant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariantResponse> getVariantsByProductId(Long productId) {
		// Ensure product exists
		if (!productRepository.existsById(productId)) {
			throw new IllegalArgumentException("Product not found with id: " + productId);
		}
		return productVariantRepository.findByProductId(productId).stream().map(productVariantMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariantResponse> getActiveVariantsByProductId(Long productId) {
		if (!productRepository.existsById(productId)) {
			throw new IllegalArgumentException("Product not found with id: " + productId);
		}
		return productVariantRepository.findByProductIdAndActiveTrue(productId).stream()
				.map(productVariantMapper::toResponse).toList();
	}

	@Override
	public ProductVariantResponse updateVariant(Long id, ProductVariantRequest request) {
		ProductVariant existingVariant = getVariantEntityById(id);
		String normalizedSku = request.getSku().trim().toUpperCase();
		// Check if the updated SKU belongs to another variant
		productVariantRepository.findBySkuIgnoreCase(normalizedSku).filter(other -> !other.getId().equals(id))
				.ifPresent(other -> {
					throw new IllegalArgumentException("Variant with SKU '" + normalizedSku + "' already exists");
				});
		String size = request.getSize() != null ? request.getSize().trim() : null;
		String color = request.getColor() != null ? request.getColor().trim() : null;
		// Check duplicate size and color on the same product (excluding this variant)
		Long productId = existingVariant.getProduct().getId();
		boolean duplicateCombination = productVariantRepository.findByProductId(productId).stream()
				.anyMatch(v -> !v.getId().equals(id) && java.util.Objects.equals(v.getSize(), size)
						&& java.util.Objects.equals(v.getColor(), color));
		if (duplicateCombination) {
			throw new IllegalArgumentException("Another variant with size '" + size + "' and color '" + color
					+ "' already exists for this product");
		}
		existingVariant.setSku(normalizedSku);
		existingVariant.setSize(size);
		existingVariant.setColor(color);
		existingVariant.setPrice(request.getPrice());
		if (request.getCurrency() != null) {
			existingVariant.setCurrency(request.getCurrency().trim().toUpperCase());
		}
		ProductVariant updatedVariant = productVariantRepository.save(existingVariant);
		return productVariantMapper.toResponse(updatedVariant);
	}

	@Override
	public ProductVariantResponse updateVariantStatus(Long id, Boolean active) {
		if (active == null) {
			throw new IllegalArgumentException("Active status cannot be null");
		}
		ProductVariant variant = getVariantEntityById(id);
		variant.setActive(active);
		ProductVariant updatedVariant = productVariantRepository.save(variant);
		return productVariantMapper.toResponse(updatedVariant);
	}

	// Helper method to fetch entity or throw exception
	private ProductVariant getVariantEntityById(Long id) {
		return productVariantRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariantResponse> getVariantsByBatchSkus(BatchSkuRequest request) {
		if (request == null || request.getSkus() == null || request.getSkus().isEmpty()) {
			throw new IllegalArgumentException("SKU list cannot be empty");
		}
		// Normalize all SKUs to uppercase trimmed strings
		List<String> normalizedSkus = request.getSkus().stream().filter(sku -> sku != null && !sku.trim().isEmpty())
				.map(sku -> sku.trim().toUpperCase()).distinct().toList();
		if (normalizedSkus.isEmpty()) {
			throw new IllegalArgumentException("No valid SKUs provided");
		}
		return productVariantRepository.findActivePublishedVariantsBySkus(normalizedSkus).stream()
				.map(productVariantMapper::toResponse).toList();
	}

}
