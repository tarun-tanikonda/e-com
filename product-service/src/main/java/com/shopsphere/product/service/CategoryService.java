package com.shopsphere.product.service;

import java.util.List;

import com.shopsphere.product.dto.request.CategoryRequest;
import com.shopsphere.product.dto.response.CategoryResponse;

public interface CategoryService {

	public CategoryResponse  createCategory(CategoryRequest request);

	public CategoryResponse  getCategoryById(Long id);

	public List<CategoryResponse > getAllCategories();

	public CategoryResponse  updateCategory(Long id, CategoryRequest request);

	public CategoryResponse  updateCategoryStatus(Long id, Boolean active);
	
	List<CategoryResponse> getCategoriesByStatus(Boolean active);

}
