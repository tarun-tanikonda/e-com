package com.shopsphere.product.service;

import java.util.List;

import com.shopsphere.product.dto.request.ProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;
import com.shopsphere.product.entity.ProductStatus;

public interface ProductService {
	
	ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id, ProductRequest request);

    ProductResponse updateProductStatus(Long id, ProductStatus status);

}
