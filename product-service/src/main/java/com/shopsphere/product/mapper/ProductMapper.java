package com.shopsphere.product.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.shopsphere.product.dto.request.ProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;
import com.shopsphere.product.dto.response.ProductVariantResponse;
import com.shopsphere.product.entity.Product;

@Component
public class ProductMapper {

	 // Convert ProductRequest DTO to Product Entity
    public Product toEntity(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());

        return product;
    }

    // Convert Product Entity to ProductResponse DTO
    public ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
	
    
    // Convert Product Entity and its variants to ProductResponse DTO
    public ProductResponse toResponse(Product product, List<ProductVariantResponse> variants) {
        ProductResponse response = toResponse(product);
        response.setVariants(variants);
        return response;
    }
}
