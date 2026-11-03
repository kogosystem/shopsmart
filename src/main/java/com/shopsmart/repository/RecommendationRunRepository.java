package com.shopsmart.repository;

import com.shopsmart.entity.RecommendationRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecommendationRunRepository extends JpaRepository<RecommendationRun, Long> {

    /** Most recent run for a customer - what the customer page displays. */
    Optional<RecommendationRun> findFirstByCustomerIdOrderByGeneratedAtDesc(Long customerId);

    /** Admin log, newest first. */
    Page<RecommendationRun> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    List<RecommendationRun> findByCustomerIdOrderByGeneratedAtDesc(Long customerId);

    /** "Total recommendations generated" dashboard card counts run items, via this count of runs. */
    @Override
    long count();
}
