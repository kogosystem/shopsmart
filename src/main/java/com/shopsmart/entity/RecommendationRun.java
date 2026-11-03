package com.shopsmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One execution of the recommendation engine for one customer.
 * Keeping each run separate gives a reproducible, timestamped snapshot that the
 * temporal evaluation can compare against later purchases.
 */
@Entity
@Table(name = "recommendation_runs",
       indexes = @Index(name = "idx_runs_customer", columnList = "customer_id"))
public class RecommendationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_run_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    /** e.g. "hybrid-rule-v1" - lets the report state which algorithm produced these rows. */
    @Column(name = "algorithm_version", nullable = false, length = 50)
    private String algorithmVersion;

    /** Optional contextual signal: the category the customer was browsing. */
    @Column(name = "context_category_id")
    private Long contextCategoryId;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Recommendation> items = new ArrayList<>();

    public RecommendationRun() { }

    public RecommendationRun(User customer, String algorithmVersion) {
        this.customer = customer;
        this.algorithmVersion = algorithmVersion;
    }

    public void addItem(Recommendation r) {
        items.add(r);
        r.setRun(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public Long getContextCategoryId() { return contextCategoryId; }
    public void setContextCategoryId(Long contextCategoryId) { this.contextCategoryId = contextCategoryId; }
    public List<Recommendation> getItems() { return items; }
    public void setItems(List<Recommendation> items) { this.items = items; }
}
