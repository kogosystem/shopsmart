package com.shopsmart.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One recommended product inside one run.
 *
 * The four component score columns are stored separately on purpose: the admin
 * recommendation page can then show exactly how the final score was built,
 * which is what makes the algorithm explainable rather than a black box.
 */


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "recommendations",
       uniqueConstraints = @UniqueConstraint(name = "uk_run_product",
                                             columnNames = {"recommendation_run_id", "product_id"}),
       indexes = @Index(name = "idx_rec_run", columnList = "recommendation_run_id"))
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_run_id", nullable = false)
    private RecommendationRun run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The final combined score used for ranking. */
    @Column(name = "score", nullable = false)
    private Double score;

    /** 1 = best recommendation. */
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    // ---- score components, kept for transparency ----
    @Column(name = "category_score")   private Double categoryScore;
    @Column(name = "popularity_score") private Double popularityScore;
    @Column(name = "feedback_score")   private Double feedbackScore;
    @Column(name = "similarity_score") private Double similarityScore;

    /** Short human-readable reason, e.g. "Because you often shop in Laptops". */
    @Column(name = "explanation", length = 255)
    private String explanation;


}
