package com.shopsmart.repository;

import com.shopsmart.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /** Ranked items of one run, best first - drives customer and admin pages. */
    List<Recommendation> findByRunIdOrderByRankPositionAsc(Long runId);

    long countByRunId(Long runId);

    /**
     * Total number of recommendation rows ever produced. This is the figure the
     * dashboard calls "Total Recommendations".
     */
    @Override
    long count();

    /** Recommendation rows with their component scores, for the admin transparency table. */
    @Query("""
           SELECT r FROM Recommendation r
           JOIN FETCH r.product p
           JOIN FETCH p.category
           WHERE r.run.id = :runId
           ORDER BY r.rankPosition ASC
           """)
    List<Recommendation> findDetailedByRunId(@Param("runId") Long runId);
}
