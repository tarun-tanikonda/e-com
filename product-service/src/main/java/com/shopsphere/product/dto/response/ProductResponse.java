package com.shopsphere.product.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.shopsphere.product.entity.ProductStatus;

public class ProductResponse {

	private Long id;
	private String name;
	private String description;
	private Long categoryId;
	private String categoryName;
	private ProductStatus status;
	private Instant createdAt;
	private Instant updatedAt;
	
	private List<ProductVariantResponse> variants = new ArrayList<>();

	public ProductResponse() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<ProductVariantResponse> getVariants() {
		return variants;
	}

	public void setVariants(List<ProductVariantResponse> variants) {
		this.variants = variants;
	}
	
	

}
