package com.shopsphere.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopsphere.product.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	List<ProductVariant> findByProductId(Long productId);

	List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

	Optional<ProductVariant> findBySku(String sku);

	boolean existsBySku(String sku);

	boolean existsByProductIdAndSizeAndColor(Long productId, String size, String color);

	Optional<ProductVariant> findBySkuIgnoreCase(String sku);

	@Query("SELECT v FROM ProductVariant v " + "WHERE UPPER(v.sku) IN :skus " + "AND v.active = true "
			+ "AND v.product.status = com.shopsphere.product.entity.ProductStatus.PUBLISHED")
	List<ProductVariant> findActivePublishedVariantsBySkus(@Param("skus") List<String> skus);

}
