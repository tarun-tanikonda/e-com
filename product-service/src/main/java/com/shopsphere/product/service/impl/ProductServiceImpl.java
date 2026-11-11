package com.shopsphere.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.product.dto.request.ProductRequest;
import com.shopsphere.product.dto.response.PagedResponse;
import com.shopsphere.product.dto.response.ProductResponse;
import com.shopsphere.product.dto.response.ProductVariantResponse;
import com.shopsphere.product.entity.Category;
import com.shopsphere.product.entity.Product;
import com.shopsphere.product.entity.ProductStatus;
import com.shopsphere.product.mapper.ProductMapper;
import com.shopsphere.product.mapper.ProductVariantMapper;
import com.shopsphere.product.repository.CategoryRepository;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.product.repository.ProductVariantRepository;
import com.shopsphere.product.service.ProductService;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductMapper productMapper;

	private final ProductVariantRepository productVariantRepository;
	private final ProductVariantMapper productVariantMapper;

	public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
			ProductMapper productMapper, ProductVariantRepository productVariantRepository,
			ProductVariantMapper productVariantMapper) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productMapper = productMapper;
		this.productVariantRepository = productVariantRepository;
		this.productVariantMapper = productVariantMapper;
	}

	@Override
	public ProductResponse createProduct(ProductRequest request) {

		// Check whether the product already exists in this category
		boolean exists = productRepository.existsByNameIgnoreCaseAndCategoryId(request.getName(),
				request.getCategoryId());

		if (exists) {
			throw new IllegalArgumentException("Product already exists in this category");
		}

		// Fetch category
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

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

//	@Override
//	@Transactional(readOnly = true)
//	public ProductResponse getProductById(Long id) {
//
//		Product product = productRepository.findById(id)
//				.orElseThrow(() -> new IllegalArgumentException("Product not found"));
//
//		return productMapper.toResponse(product);
//	}

	@Override
	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
		// Fetch all active variants for this product
		List<ProductVariantResponse> activeVariants = productVariantRepository.findByProductIdAndActiveTrue(id).stream()
				.map(productVariantMapper::toResponse).toList();
		// Return ProductResponse with nested variants
		return productMapper.toResponse(product, activeVariants);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponse> getAllProducts() {

		return productRepository.findAll().stream().map(productMapper::toResponse).toList();
	}

	@Override
	public ProductResponse updateProduct(Long id, ProductRequest request) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		if (!Boolean.TRUE.equals(category.getActive())) {
			throw new IllegalArgumentException("Cannot assign product to an inactive category");
		}

		// Check duplicate name, excluding the current product
		boolean duplicateExists = productRepository.existsByNameIgnoreCaseAndCategoryId(request.getName(),
				request.getCategoryId());

		if (duplicateExists && !(product.getName().equalsIgnoreCase(request.getName())
				&& product.getCategory().getId().equals(request.getCategoryId()))) {

			throw new IllegalArgumentException("Another product with this name already exists in this category");
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
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		product.setStatus(status);

		Product updatedProduct = productRepository.save(product);

		return productMapper.toResponse(updatedProduct);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ProductResponse> getPublishedCatalog(Long categoryId, String keyword, int page, int size,
			String sortBy, String sortDir) {

		// 1. Sanitize pagination bounds (enforces SRS business rule: bounded page size)
		int validatedPage = Math.max(0, page);
		int validatedSize = (size <= 0) ? 10 : Math.min(size, 50); // Minimum 1, Maximum 50 items

		// 2. Validate sort fields to prevent PropertyReferenceException
		java.util.Set<String> allowedSortFields = java.util.Set.of("id", "name", "createdAt", "updatedAt");
		String validatedSortBy = (sortBy != null && allowedSortFields.contains(sortBy)) ? sortBy : "createdAt";
		org.springframework.data.domain.Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
				? org.springframework.data.domain.Sort.Direction.ASC
				: org.springframework.data.domain.Sort.Direction.DESC;
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
				.of(validatedPage, validatedSize, org.springframework.data.domain.Sort.by(direction, validatedSortBy));

		// 3. Clean search keyword
		String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

		// 4. Query repository - strictly filtering by PUBLISHED status
		org.springframework.data.domain.Page<Product> productPage = productRepository
				.searchCatalog(ProductStatus.PUBLISHED, categoryId, cleanKeyword, pageable);

		// 5. Map entities to DTOs
		List<ProductResponse> content = productPage.getContent().stream().map(productMapper::toResponse).toList();

		// 6. Wrap in custom PagedResponse envelope
		return new PagedResponse<>(content, productPage.getNumber(), productPage.getSize(),
				productPage.getTotalElements(), productPage.getTotalPages(), productPage.isLast());
	}

}
