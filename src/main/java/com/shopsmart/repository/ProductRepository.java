package com.shopsmart.repository;

import com.shopsmart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/** Database access for products, including the search used by the catalogue. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Products that can be sold or recommended: active, in stock, in an active category. */
    @Query("""
           SELECT p FROM Product p JOIN p.category c
           WHERE p.active = true AND p.stock > 0 AND c.active = true
           """)
    List<Product> findSellable();

    /** Search across name and description, filtered by active + optional category. */
    @Query("""
           SELECT p FROM Product p JOIN p.category c
           WHERE p.active = true
             AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:categoryId IS NULL OR c.id = :categoryId)
           """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         Pageable pageable);

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Product> findByActiveTrue();

    long countByActiveTrue();

    /** The admin "deactivate safely" check: is this product referenced by any historical order? */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId")
    long countOrderReferences(@Param("productId") Long productId);

    /** How many products belong to a category - used before deactivating a category. */
    long countByCategoryId(Long categoryId);
}
