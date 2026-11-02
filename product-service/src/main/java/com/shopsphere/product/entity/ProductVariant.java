package com.shopsphere.product.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "product_variants", schema = "product_schema", indexes = {
		@Index(name = "idx_variant_product", columnList = "product_id") }, uniqueConstraints = {
				@UniqueConstraint(name = "uk_variant_sku", columnNames = { "sku" }),
				@UniqueConstraint(name = "uk_product_size_color", columnNames = { "product_id", "size", "color" }) })
public class ProductVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "sku", nullable = false, length = 100)
	private String sku;

	@Column(name = "size", length = 20)
	private String size;

	@Column(name = "color", length = 50)
	private String color;

	@Column(name = "price", nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency = "INR";

	@Column(name = "active", nullable = false)
	private Boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}
	
	
	public ProductVariant() {
	}

	// Getters and setters

	public Long getId() {
	    return id;
	}

	public void setId(Long id) {
	    this.id = id;
	}

	public Product getProduct() {
	    return product;
	}

	public void setProduct(Product product) {
	    this.product = product;
	}

	public String getSku() {
	    return sku;
	}

	public void setSku(String sku) {
	    this.sku = sku;
	}

	public String getSize() {
	    return size;
	}

	public void setSize(String size) {
	    this.size = size;
	}

	public String getColor() {
	    return color;
	}

	public void setColor(String color) {
	    this.color = color;
	}

	public BigDecimal getPrice() {
	    return price;
	}

	public void setPrice(BigDecimal price) {
	    this.price = price;
	}

	public String getCurrency() {
	    return currency;
	}

	public void setCurrency(String currency) {
	    this.currency = currency;
	}

	public Boolean getActive() {
	    return active;
	}

	public void setActive(Boolean active) {
	    this.active = active;
	}

	public Instant getCreatedAt() {
	    return createdAt;
	}

	public Instant getUpdatedAt() {
	    return updatedAt;
	}

}
