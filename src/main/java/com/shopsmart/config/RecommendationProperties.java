package com.shopsmart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * All recommendation scoring weights in ONE place, bound to the
 * "shopsmart.recommendation.*" lines in application.properties.
 *
 * This is what makes the weights "configurable without touching Java code"
 * (spec section 98): to re-tune the engine you edit application.properties and
 * restart - no recompile. Each weight is documented where it is used in
 * RecommendationService.
 */
@Component
@ConfigurationProperties(prefix = "shopsmart.recommendation")
public class RecommendationProperties {

    /** How many products to recommend (the "top N"). */
    private int topN = 8;

    /** Weight for "this product is in a category you often buy". Highest, because
     *  category preference is the strongest personal signal we have. */
    private double categoryWeight = 3.0;

    /** Weight for overall popularity. Moderate: popular items are a safe bet but
     *  must not drown out personal preference. */
    private double popularityWeight = 1.5;

    /** Weight for the customer's own likes/dislikes. Second highest, because an
     *  explicit like is a very direct signal. */
    private double feedbackWeight = 2.0;

    /** Weight for "customers who buy your categories also buy this" similarity. */
    private double similarityWeight = 1.0;

    /** Small bonus for being well in stock, so near-empty items rank slightly lower. */
    private double availabilityWeight = 0.5;

    /** Written onto every run so the report can say which algorithm produced it. */
    private String algorithmVersion = "hybrid-rule-v1";

    public int getTopN() { return topN; }
    public void setTopN(int topN) { this.topN = topN; }
    public double getCategoryWeight() { return categoryWeight; }
    public void setCategoryWeight(double categoryWeight) { this.categoryWeight = categoryWeight; }
    public double getPopularityWeight() { return popularityWeight; }
    public void setPopularityWeight(double popularityWeight) { this.popularityWeight = popularityWeight; }
    public double getFeedbackWeight() { return feedbackWeight; }
    public void setFeedbackWeight(double feedbackWeight) { this.feedbackWeight = feedbackWeight; }
    public double getSimilarityWeight() { return similarityWeight; }
    public void setSimilarityWeight(double similarityWeight) { this.similarityWeight = similarityWeight; }
    public double getAvailabilityWeight() { return availabilityWeight; }
    public void setAvailabilityWeight(double availabilityWeight) { this.availabilityWeight = availabilityWeight; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
}
