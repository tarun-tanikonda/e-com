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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.product.dto.request.ProductVariantRequest;
import com.shopsphere.product.dto.response.ProductVariantResponse;
import com.shopsphere.product.service.ProductVariantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/variants")
public class ProductVariantController {

	private final ProductVariantService productVariantService;

	public ProductVariantController(ProductVariantService productVariantService) {
		this.productVariantService = productVariantService;
	}

	// 1. Add Variant to Product
	@PostMapping("/add/{productId}")
	public ResponseEntity<?> createVariant(@PathVariable Long productId,
			@Valid @RequestBody ProductVariantRequest request) {
		try {
			ProductVariantResponse response = productVariantService.createVariant(productId, request);
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while creating the variant");
		}
	}

	// 2. Get Variant By ID
	@GetMapping("/get/{id}")
	public ResponseEntity<?> getVariantById(@PathVariable Long id) {
		try {
			ProductVariantResponse response = productVariantService.getVariantById(id);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving the variant");
		}
	}

	// 3. Get Variant By SKU (Used by Cart & Order Services)
	@GetMapping("/sku/{sku}")
	public ResponseEntity<?> getVariantBySku(@PathVariable String sku) {
		try {
			ProductVariantResponse response = productVariantService.getVariantBySku(sku);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving the variant by SKU");
		}
	}

	// 4. Get All Variants for a Product
	@GetMapping("/product/{productId}")
	public ResponseEntity<?> getVariantsByProductId(@PathVariable Long productId) {
		try {
			List<ProductVariantResponse> response = productVariantService.getVariantsByProductId(productId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving variants for product");
		}
	}

	// 5. Get Active Variants for a Product (Public Catalog View)
	@GetMapping("/product/{productId}/active")
	public ResponseEntity<?> getActiveVariantsByProductId(@PathVariable Long productId) {
		try {
			List<ProductVariantResponse> response = productVariantService.getActiveVariantsByProductId(productId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while retrieving active variants");
		}
	}

	// 6. Update Variant
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateVariant(@PathVariable Long id, @Valid @RequestBody ProductVariantRequest request) {
		try {
			ProductVariantResponse response = productVariantService.updateVariant(id, request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while updating the variant");
		}
	}

	// 7. Update Variant Active Status
	@PatchMapping("/update/{id}/status")
	public ResponseEntity<?> updateVariantStatus(@PathVariable Long id, @RequestParam Boolean active) {
		try {
			ProductVariantResponse response = productVariantService.updateVariantStatus(id, active);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while updating variant status");
		}
	}

	// 8. Batch SKU Lookup (For Cart & Order Services)
	@PostMapping("/batch-validate")
	public ResponseEntity<?> getVariantsByBatchSkus(
			@Valid @RequestBody com.shopsphere.product.dto.request.BatchSkuRequest request) {
		try {
			List<ProductVariantResponse> response = productVariantService.getVariantsByBatchSkus(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while validating batch SKUs");
		}
	}

}
