package com.shopsphere.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.product.dto.request.ProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;
import com.shopsphere.product.entity.ProductStatus;
import com.shopsphere.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
//@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	// Create Product
	@PostMapping("/add")
	public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {

		try {
			ProductResponse response = productService.createProduct(request);

			return new ResponseEntity<>(response, HttpStatus.CREATED);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while creating the product");
		}
	}

	// Get Product By ID
	@GetMapping("/get/{id}")
	public ResponseEntity<?> getProductById(@PathVariable Long id) {

		try {
			ProductResponse response = productService.getProductById(id);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving the product");
		}
	}

	// Get All Products
	@GetMapping("/getAll")
	public ResponseEntity<?> getAllProducts() {

		try {
			List<ProductResponse> response = productService.getAllProducts();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving products");
		}
	}

	// Update Product
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {

		try {
			ProductResponse response = productService.updateProduct(id, request);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while updating the product");
		}
	}

	// Update Product Status
	@PatchMapping("/update/{id}/status")
	public ResponseEntity<?> updateProductStatus(@PathVariable Long id, @RequestParam ProductStatus status) {

		try {
			ProductResponse response = productService.updateProductStatus(id, status);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while updating product status");
		}
	}

	// Public Catalogue: Search, Filter & Pagination
	@GetMapping("/catalog")
	public ResponseEntity<?> getPublishedCatalog(@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		try {
			com.shopsphere.product.dto.response.PagedResponse<ProductResponse> response = productService
					.getPublishedCatalog(categoryId, keyword, page, size, sortBy, sortDir);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving the catalog");
		}
	}

}
