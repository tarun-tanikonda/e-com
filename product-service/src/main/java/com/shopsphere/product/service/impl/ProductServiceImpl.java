package com.shopsphere.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.product.dto.request.ProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;
import com.shopsphere.product.entity.Category;
import com.shopsphere.product.entity.Product;
import com.shopsphere.product.entity.ProductStatus;
import com.shopsphere.product.mapper.ProductMapper;
import com.shopsphere.product.repository.CategoryRepository;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.product.service.ProductService;


@Service
@Transactional
public class ProductServiceImpl implements ProductService {
	
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }
    
    @Override
    public ProductResponse createProduct(ProductRequest request) {

        // Check whether the product already exists in this category
        boolean exists = productRepository.existsByNameIgnoreCaseAndCategoryId(
                request.getName(), request.getCategoryId());

        if (exists) {
            throw new IllegalArgumentException(
                    "Product already exists in this category");
        }

        // Fetch category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException("Cannot create product under an inactive category");
        }

        // Convert request DTO to entity
        Product product = productMapper.toEntity(request);

        // Set category and default status
        product.setCategory(category);
        product.setStatus(ProductStatus.DRAFT);

        // Save product
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        return productMapper.toResponse(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
    
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException("Cannot assign product to an inactive category");
        }

        // Check duplicate name, excluding the current product
        boolean duplicateExists = productRepository
                .existsByNameIgnoreCaseAndCategoryId(
                        request.getName(), request.getCategoryId());

        if (duplicateExists && 
                !(product.getName().equalsIgnoreCase(request.getName())
                && product.getCategory().getId().equals(request.getCategoryId()))) {

            throw new IllegalArgumentException(
                    "Another product with this name already exists in this category");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }
    
    @Override
    public ProductResponse updateProductStatus(Long id, ProductStatus status) {

        if (status == null) {
            throw new IllegalArgumentException("Product status is required");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        product.setStatus(status);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

}
