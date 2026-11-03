package com.shopsmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One line of an order. This table IS the purchase history that feeds the
 * recommendation engine and the evaluation metrics.
 *
 * productName, unitPrice and categoryId are SNAPSHOTS taken at purchase time:
 * the recommender can read them without joining back to products, so historical
 * data stays valid even if a product is later re-priced or deactivated.
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items",
       indexes = {
           @Index(name = "idx_order_items_order", columnList = "order_id"),
           @Index(name = "idx_order_items_product", columnList = "product_id")
       })
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Which product was bought. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Snapshot of the product name at purchase time. */
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    /** Snapshot of the category id at purchase time (drives category preference). */
    @Column(name = "category_id")
    private Long categoryId;

    /** Snapshot of the price at purchase time. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;


}
