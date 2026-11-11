package com.shopsphere.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopsphere.product.entity.Product;
import com.shopsphere.product.entity.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByStatus(ProductStatus status);

	List<Product> findByCategoryId(Long categoryId);

	List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

	boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

	@Query("SELECT p FROM Product p WHERE p.status = :status "
			+ "AND (:categoryId IS NULL OR p.category.id = :categoryId) "
			+ "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<Product> searchCatalog(@Param("status") ProductStatus status, @Param("categoryId") Long categoryId,
			@Param("keyword") String keyword, Pageable pageable);

}
