package com.shopsmart.repository;

import com.shopsmart.entity.Feedback;
import com.shopsmart.entity.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/** Database access for customer feedback - the input to the feedback loop. */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    long countByType(FeedbackType type);

    /** A customer's own feedback (the feedback page). */
    List<Feedback> findByCustomerIdOrderByFeedbackDateDesc(Long customerId);

    /** Admin feedback log, newest first. */
    Page<Feedback> findAllByOrderByFeedbackDateDesc(Pageable pageable);

    /**
     * Net feedback per product for a customer: likes minus dislikes.
     * Object[] = {productId, netScore}. The recommender adds this to a product's
     * score, so a previously-liked product tends to appear higher next time.
     */
    @Query("""
           SELECT f.product.id,
                  SUM(CASE WHEN f.type = com.shopsmart.entity.FeedbackType.LIKE THEN 1 ELSE -1 END)
           FROM Feedback f
           WHERE f.customer.id = :customerId
           GROUP BY f.product.id
           """)
    List<Object[]> findNetFeedbackPerProduct(@Param("customerId") Long customerId);

    /** Has this customer already given feedback on this product? Prevents duplicates. */
    java.util.Optional<Feedback> findByCustomerIdAndProductId(Long customerId, Long productId);
}
