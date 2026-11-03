package com.shopsmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** A customer's LIKE/DISLIKE on a product, optionally on a specific recommendation run. */
@Entity
@Table(name = "feedback",
       indexes = {
           @Index(name = "idx_feedback_customer", columnList = "customer_id"),
           @Index(name = "idx_feedback_product", columnList = "product_id")
       })
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 10)
    private FeedbackType type;

    @Size(max = 500, message = "Comment must be 500 characters or fewer")
    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "feedback_date", nullable = false)
    private LocalDateTime feedbackDate = LocalDateTime.now();

    /**
     * Optional link to the run that surfaced this product, so feedback can be
     * tied to a specific recommendation event (the feedback loop).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_run_id")
    private RecommendationRun run;

    public Feedback() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public FeedbackType getType() { return type; }
    public void setType(FeedbackType type) { this.type = type; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public LocalDateTime getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(LocalDateTime feedbackDate) { this.feedbackDate = feedbackDate; }
    public RecommendationRun getRun() { return run; }
    public void setRun(RecommendationRun run) { this.run = run; }
}
