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

import com.shopsphere.product.dto.request.CategoryRequest;
import com.shopsphere.product.dto.response.CategoryResponse;
import com.shopsphere.product.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("categories")
public class CategoryController {

	
	private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    // Create Category
    @PostMapping(value="add")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.createCategory(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    
    // Get Category By ID
    @GetMapping("get/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id) {

        CategoryResponse response = categoryService.getCategoryById(id);

        return ResponseEntity.ok(response);
    }

    // Get All Categories
    @GetMapping("getAll")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    // Update Category
    @PutMapping("update/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(response);
    }

    // Update Category Status
    @PatchMapping("update/{id}/status")
    public ResponseEntity<CategoryResponse> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam Boolean active) {

        CategoryResponse response = categoryService.updateCategoryStatus(id, active);

        return ResponseEntity.ok(response);
    }
    
 // Get Categories By Status
    @GetMapping("getByStatus")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByStatus(
            @RequestParam Boolean active) {

        List<CategoryResponse> categories =
                categoryService.getCategoriesByStatus(active);

        return ResponseEntity.ok(categories);
    }
}
