package com.shopsphere.product.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class BatchSkuRequest {

	@NotEmpty(message = "SKU list cannot be empty")
	private List<String> skus;

	public BatchSkuRequest() {
	}

	public List<String> getSkus() {
		return skus;
	}

	public void setSkus(List<String> skus) {
		this.skus = skus;
	}
	
	

}
