package com.shopsphere.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsphere.product.entity.Product;
import com.shopsphere.product.entity.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByStatus(ProductStatus status);

	List<Product> findByCategoryId(Long categoryId);

	List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

	boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

}
