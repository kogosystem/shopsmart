package com.shopsmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/** One line in a cart. A unique constraint stops the same product appearing twice. */
@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(name = "uk_cart_product",
                                             columnNames = {"cart_id", "product_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    public CartItem() { }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    @Transient
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
