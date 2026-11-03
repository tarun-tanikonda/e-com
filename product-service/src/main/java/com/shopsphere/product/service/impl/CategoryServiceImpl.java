package com.shopsphere.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.product.dto.request.CategoryRequest;
import com.shopsphere.product.dto.response.CategoryResponse;
import com.shopsphere.product.entity.Category;
import com.shopsphere.product.mapper.CategoryMapper;
import com.shopsphere.product.repository.CategoryRepository;
import com.shopsphere.product.service.CategoryService;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {

		this.categoryRepository = categoryRepository;
		this.categoryMapper = categoryMapper;

	}

	@Override
	public CategoryResponse createCategory(CategoryRequest request) {

		if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
			throw new IllegalArgumentException("Category already exists");
		}

		Category category = categoryMapper.toEntity(request);

		if (request.getParentCategoryId() != null) {
			Category parentCategory = getCategoryEntityById(request.getParentCategoryId());
			category.setParentCategory(parentCategory);
		}

		Category savedCategory = categoryRepository.save(category);

		return categoryMapper.toResponse(savedCategory);
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(Long id) {

		Category category = getCategoryEntityById(id);

		return categoryMapper.toResponse(category);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategories() {

		return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
	}

	@Override
	public CategoryResponse updateCategory(Long id, CategoryRequest request) {

		Category existingCategory = getCategoryEntityById(id);

		categoryRepository.findByNameIgnoreCase(request.getName()).filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Category name already exists");
				});

		existingCategory.setName(request.getName());
		existingCategory.setDescription(request.getDescription());

		if (request.getParentCategoryId() != null) {

			if (request.getParentCategoryId().equals(id)) {
				throw new IllegalArgumentException("Category cannot be its own parent");
			}

			Category parentCategory = getCategoryEntityById(request.getParentCategoryId());

			existingCategory.setParentCategory(parentCategory);
		}

		Category updatedCategory = categoryRepository.save(existingCategory);

		return categoryMapper.toResponse(updatedCategory);
	}

	@Override
	public CategoryResponse updateCategoryStatus(Long id, Boolean active) {

		if (active == null) {
			throw new IllegalArgumentException("Active status cannot be null");
		}

		Category category = getCategoryEntityById(id);

		category.setActive(active);

		Category updatedCategory = categoryRepository.save(category);

		return categoryMapper.toResponse(updatedCategory);
	}

	// Internal helper method: returns Entity, not DTO
	private Category getCategoryEntityById(Long id) {

		return categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponse> getCategoriesByStatus(Boolean active) {

	    return categoryRepository.findByActive(active)
	            .stream()
	            .map(categoryMapper::toResponse)
	            .toList();
	}
}
