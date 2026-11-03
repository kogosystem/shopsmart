package com.shopsmart.repository;

import com.shopsmart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Find an existing line so that adding the same product increases quantity. */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
