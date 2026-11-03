package com.shopsmart.repository;

import com.shopsmart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Database access for order lines. These queries are the backbone of the
 * recommendation engine: they tell us what each customer bought, when, and how
 * popular each product is overall.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Product ids a customer has ALREADY bought (before the cut-off).
     * The recommender excludes these so it never suggests something the
     * customer already owns - one of the core problems this project addresses.
     */
    @Query("""
           SELECT DISTINCT oi.product.id FROM OrderItem oi
           WHERE oi.order.customer.id = :customerId
             AND oi.order.status <> 'CANCELLED'
             AND oi.order.orderDate < :cutoff
           """)
    List<Long> findPurchasedProductIds(@Param("customerId") Long customerId,
                                       @Param("cutoff") LocalDateTime cutoff);

    /**
     * How many times the customer bought in each category.
     * Object[] = {categoryId, count}. The biggest count is the preferred category.
     */
    @Query("""
           SELECT oi.categoryId, COUNT(oi) FROM OrderItem oi
           WHERE oi.order.customer.id = :customerId
             AND oi.categoryId IS NOT NULL
             AND oi.order.status <> 'CANCELLED'
             AND oi.order.orderDate < :cutoff
           GROUP BY oi.categoryId
           """)
    List<Object[]> findCategoryPurchaseCounts(@Param("customerId") Long customerId,
                                              @Param("cutoff") LocalDateTime cutoff);

    /** Same idea but for one category over ALL customers - the product similarity signal. */
    @Query("""
           SELECT COUNT(DISTINCT oi.order.customer.id) FROM OrderItem oi
           WHERE oi.categoryId = :categoryId
             AND oi.order.status <> 'CANCELLED'
             AND oi.order.orderDate < :cutoff
           """)
    long countDistinctCustomersInCategory(@Param("categoryId") Long categoryId,
                                          @Param("cutoff") LocalDateTime cutoff);

    /** Popularity signal: total units of each product sold (before the cut-off). */
    @Query("""
           SELECT oi.product.id, COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi
           WHERE oi.order.status <> 'CANCELLED'
             AND oi.order.orderDate < :cutoff
           GROUP BY oi.product.id
           """)
    List<Object[]> findProductSalesTotals(@Param("cutoff") LocalDateTime cutoff);

    /** Everything a customer bought in a window - used by the evaluation's ground truth. */
    @Query("""
           SELECT oi FROM OrderItem oi
           WHERE oi.order.customer.id = :customerId
             AND oi.order.status <> 'CANCELLED'
             AND oi.order.orderDate >= :from
             AND oi.order.orderDate < :to
           """)
    List<OrderItem> findByCustomerAndWindow(@Param("customerId") Long customerId,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    /** Newest first purchase date for a customer - lets us place the evaluation cut-off. */
    @Query("""
           SELECT MIN(oi.order.orderDate) FROM OrderItem oi
           WHERE oi.order.customer.id = :customerId AND oi.order.status <> 'CANCELLED'
           """)
    LocalDateTime findFirstPurchaseDate(@Param("customerId") Long customerId);
}
