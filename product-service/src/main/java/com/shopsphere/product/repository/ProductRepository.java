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

	Page<Product> findByStatus(ProductStatus status, Pageable pageable);

	List<Product> findByCategoryId(Long categoryId);

	Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

	List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

	boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

	@Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = :status "
			+ "AND (CAST(:categoryId AS Long) IS NULL OR p.category.id = :categoryId) "
			+ "AND (CAST(:keyword AS String) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))",
		countQuery = "SELECT count(p) FROM Product p WHERE p.status = :status "
			+ "AND (CAST(:categoryId AS Long) IS NULL OR p.category.id = :categoryId) "
			+ "AND (CAST(:keyword AS String) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<Product> searchCatalog(@Param("status") ProductStatus status, @Param("categoryId") Long categoryId,
			@Param("keyword") String keyword, Pageable pageable);

}
