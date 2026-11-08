package com.shopsphere.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductRequest {
	
	   @NotBlank(message = "Product name is required")
	    @Size(max = 200, message = "Product name cannot exceed 200 characters")
	    private String name;

	    private String description;

	    @NotNull(message = "Category ID is required")
	    @Positive(message = "Category ID must be positive")
	    private Long categoryId;

	    public ProductRequest() {
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
	    
	    

}
