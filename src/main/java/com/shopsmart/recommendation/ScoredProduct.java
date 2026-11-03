package com.shopsmart.recommendation;

import com.shopsmart.entity.Product;

/**
 * A candidate product together with the five component scores and the final
 * combined score. Carrying the components (not just the total) is what lets the
 * admin page explain WHY a product was recommended.
 */
public class ScoredProduct {

    private final Product product;
    private final double categoryScore;
    private final double popularityScore;
    private final double feedbackScore;
    private final double similarityScore;
    private final double availabilityScore;
    private final double finalScore;
    private final String explanation;

    public ScoredProduct(Product product, double categoryScore, double popularityScore,
                         double feedbackScore, double similarityScore, double availabilityScore,
                         String explanation) {
        this.product = product;
        this.categoryScore = categoryScore;
        this.popularityScore = popularityScore;
        this.feedbackScore = feedbackScore;
        this.similarityScore = similarityScore;
        this.availabilityScore = availabilityScore;
        this.finalScore = categoryScore + popularityScore + feedbackScore
                        + similarityScore + availabilityScore;
        this.explanation = explanation;
    }

    public Product getProduct() { return product; }
    public double getCategoryScore() { return categoryScore; }
    public double getPopularityScore() { return popularityScore; }
    public double getFeedbackScore() { return feedbackScore; }
    public double getSimilarityScore() { return similarityScore; }
    public double getAvailabilityScore() { return availabilityScore; }
    public double getFinalScore() { return finalScore; }
    public String getExplanation() { return explanation; }
}
