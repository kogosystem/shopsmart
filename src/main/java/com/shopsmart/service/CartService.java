package com.shopsmart.service;

import com.shopsmart.entity.*;
import com.shopsmart.exception.BusinessException;
import com.shopsmart.repository.CartItemRepository;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The database-backed shopping cart. Every rule the spec asked for is enforced
 * HERE in the service, not in the browser: no inactive products, no zero/
 * negative quantities, never more than the available stock.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /** Get the customer's cart, creating an empty one the first time. */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    /** The number shown on the navbar cart badge. */
    @Transactional(readOnly = true)
    public int itemCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        return cart == null ? 0 : cart.getItemCount();
    }

    /**
     * Add a product (or increase its quantity if already in the cart).
     * Validates the product is real, active and has enough stock.
     */
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be at least 1.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("That product does not exist."));
        if (!product.isActive()) {
            throw new BusinessException("Sorry, that product is no longer available.");
        }

        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;
        if (newQuantity > product.getStock()) {
            throw new BusinessException("Only " + product.getStock()
                    + " of " + product.getName() + " are in stock.");
        }

        if (item == null) {
            item = new CartItem(product, newQuantity);
            cart.addItem(item);
        } else {
            item.setQuantity(newQuantity);
        }
        cartItemRepository.save(item);
    }

    /** Set an exact quantity for one line (used by the +/- controls on the cart page). */
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("Cart item not found."));
        // Authorisation: the item must belong to THIS user's cart.
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException("That cart item is not yours.");
        }
        if (quantity <= 0) {
            // Treat a drop to zero as "remove".
            removeItem(userId, cartItemId);
            return;
        }
        if (quantity > item.getProduct().getStock()) {
            throw new BusinessException("Only " + item.getProduct().getStock() + " in stock.");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("Cart item not found."));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException("That cart item is not yours.");
        }
        Cart cart = item.getCart();
        cart.removeItem(item);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
