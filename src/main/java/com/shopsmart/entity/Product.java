package com.shopsmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A product for sale. Prices use BigDecimal so money is exact. */
@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "products",
       indexes = {
           @Index(name = "idx_products_category", columnList = "category_id"),
           @Index(name = "idx_products_active", columnList = "active")
       })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    /** Just the file name, e.g. "laptop.jpg". The image lives in static/images/products/. */
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull(message = "Please choose a category")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;



    /** True when the product can currently be sold. */
    @Transient
    public boolean isAvailable() { return active && stock != null && stock > 0; }

    public boolean isActive() { return active; }

}
