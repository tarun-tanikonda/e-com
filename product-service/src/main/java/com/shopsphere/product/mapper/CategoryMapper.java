package com.shopsphere.product.mapper;

import org.springframework.stereotype.Component;

import com.shopsphere.product.dto.request.CategoryRequest;
import com.shopsphere.product.dto.response.CategoryResponse;
import com.shopsphere.product.entity.Category;

@Component
public class CategoryMapper {
	
    // Convert Request DTO to Entity
    public Category toEntity(CategoryRequest request) {

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return category;
    }
    
    
    // Convert Entity to Response DTO
    public CategoryResponse toResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        if (category.getParentCategory() != null) {
            response.setParentCategoryId(
                    category.getParentCategory().getId());
        }

        response.setActive(category.getActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }

}
