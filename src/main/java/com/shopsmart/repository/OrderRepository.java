package com.shopsmart.repository;

import com.shopsmart.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Database access for orders. */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** A customer's own orders, newest first (order history page). */
    Page<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId, Pageable pageable);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    /** All orders for the admin order-management screen, newest first. */
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    long countByStatusNot(com.shopsmart.entity.OrderStatus status);

    /**
     * Total sales. CANCELLED orders are excluded because they were never really
     * sold - counting them would inflate the figure on the dashboard.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    BigDecimal totalSales();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status <> 'CANCELLED'")
    long countSellableOrders();

    /** Average order value = total sales / number of orders. */
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    BigDecimal averageOrderValue();

    /**
     * Orders placed by a customer BEFORE a cut-off time. The temporal evaluation
     * uses this to build a training window that does not leak future purchases.
     */
    @Query("""
           SELECT o FROM Order o
           WHERE o.customer.id = :customerId
             AND o.orderDate < :cutoff
             AND o.status <> 'CANCELLED'
           """)
    List<Order> findByCustomerBefore(@Param("customerId") Long customerId,
                                     @Param("cutoff") LocalDateTime cutoff);

    /** Distinct customer ids that have at least one order - for evaluation sampling. */
    @Query("SELECT DISTINCT o.customer.id FROM Order o WHERE o.status <> 'CANCELLED'")
    List<Long> findDistinctCustomerIdsWithOrders();

    /** Earliest and latest order dates - the evaluation uses them to pick a split point. */
    @Query("SELECT MIN(o.orderDate) FROM Order o WHERE o.status <> 'CANCELLED'")
    LocalDateTime findMinOrderDate();

    @Query("SELECT MAX(o.orderDate) FROM Order o WHERE o.status <> 'CANCELLED'")
    LocalDateTime findMaxOrderDate();

    /** Simple "recent orders" block on the customer home page. */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<Order> findRecentByCustomer(@Param("customerId") Long customerId, Pageable pageable);
}
