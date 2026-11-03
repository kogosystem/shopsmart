package com.shopsmart.service;

import com.shopsmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gathers the headline numbers for the admin dashboard cards. Every figure is a
 * real COUNT or SUM from the database - nothing here is invented, which is why
 * the spec insisted the charts use actual data.
 */
@Service
public class DashboardService {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final FeedbackService feedbackService;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationRunRepository runRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DashboardService(UserService userService, ProductService productService,
                            CategoryService categoryService, OrderService orderService,
                            FeedbackService feedbackService,
                            RecommendationRepository recommendationRepository,
                            RecommendationRunRepository runRepository,
                            CategoryRepository categoryRepository,
                            ProductRepository productRepository) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.feedbackService = feedbackService;
        this.recommendationRepository = recommendationRepository;
        this.runRepository = runRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalCustomers", userService.countCustomers());
        m.put("totalProducts", productService.countActive());
        m.put("totalCategories", categoryService.count());
        m.put("totalOrders", orderService.countOrders());
        m.put("totalSales", orderService.totalSales());
        m.put("averageOrderValue", orderService.averageOrderValue());
        m.put("totalRecommendations", recommendationRepository.count());
        m.put("totalRuns", runRepository.count());
        m.put("totalFeedback", feedbackService.countAll());
        m.put("likes", feedbackService.countLikes());
        m.put("dislikes", feedbackService.countDislikes());
        return m;
    }

    /**
     * Data for the "products per category" bar chart on the dashboard.
     * Returns category name -> number of active products. Chart.js reads this.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> productsPerCategory() {
        Map<String, Long> data = new LinkedHashMap<>();
        categoryRepository.findByActiveTrueOrderByName().forEach(c ->
                data.put(c.getName(), productRepository.countByCategoryId(c.getId())));
        return data;
    }

    /** Likes vs dislikes for the feedback doughnut chart. */
    @Transactional(readOnly = true)
    public Map<String, Long> feedbackBreakdown() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("Likes", feedbackService.countLikes());
        data.put("Dislikes", feedbackService.countDislikes());
        return data;
    }
}
