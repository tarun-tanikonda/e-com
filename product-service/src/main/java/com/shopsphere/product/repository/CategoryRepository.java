package com.shopsphere.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsphere.product.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>  {
	
	Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByActiveTrue();
    
    List<Category> findByActive(Boolean active);

}
