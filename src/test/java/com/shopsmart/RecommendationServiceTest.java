package com.shopsmart;

import com.shopsmart.config.RecommendationProperties;
import com.shopsmart.entity.Category;
import com.shopsmart.entity.Product;
import com.shopsmart.recommendation.RecommendationService;
import com.shopsmart.recommendation.ScoredProduct;
import com.shopsmart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the core recommendation logic using mocked repositories, so no
 * database is needed. These verify the two most important research behaviours:
 *   1. products the customer already bought are never recommended, and
 *      preferred-category products rank above unrelated ones;
 *   2. a brand-new customer (cold start) still gets popular products.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationServiceTest {

    @Mock ProductRepository productRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock FeedbackRepository feedbackRepository;
    @Mock RecommendationRunRepository runRepository;
    @Mock UserRepository userRepository;

    RecommendationService service;
    LocalDateTime now = LocalDateTime.now();

    private Category laptops;
    private Category groceries;

    @BeforeEach
    void setUp() {
        RecommendationProperties props = new RecommendationProperties(); // sensible defaults
        service = new RecommendationService(productRepository, orderItemRepository,
                feedbackRepository, runRepository, userRepository, props);

        laptops = new Category("Laptops", "");    laptops.setId(10L);
        groceries = new Category("Groceries", ""); groceries.setId(20L);
    }

    private Product product(long id, Category c, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName("P" + id);
        p.setPrice(new BigDecimal("1000"));
        p.setStock(stock);
        p.setActive(true);
        p.setCategory(c);
        return p;
    }

    @Test
    void excludesPurchasedAndRanksPreferredCategoryHigher() {
        Product bought   = product(1L, laptops, 5);   // already purchased -> must be excluded
        Product newLaptop = product(2L, laptops, 10);  // preferred category -> should rank high
        Product grocery   = product(3L, groceries, 10);// unrelated -> should rank lower

        when(orderItemRepository.findPurchasedProductIds(1L, now)).thenReturn(List.of(1L));
        // Customer bought once in the Laptops category (id 10).
        when(orderItemRepository.findCategoryPurchaseCounts(1L, now))
                .thenReturn(List.<Object[]>of(new Object[]{10L, 1L}));
        when(orderItemRepository.findProductSalesTotals(now)).thenReturn(List.of());
        when(feedbackRepository.findNetFeedbackPerProduct(1L)).thenReturn(List.of());
        when(productRepository.findSellable()).thenReturn(List.of(bought, newLaptop, grocery));
        when(orderItemRepository.countDistinctCustomersInCategory(10L, now)).thenReturn(1L);
        when(userRepository.countByRoleName("ROLE_CUSTOMER")).thenReturn(1L);

        List<ScoredProduct> result = service.rank(1L, now, null, 8);

        List<Long> ids = result.stream().map(sp -> sp.getProduct().getId()).toList();
        assertThat(ids).doesNotContain(1L);                 // purchased excluded
        assertThat(ids).containsExactly(2L, 3L);            // laptop ranked above grocery
        assertThat(result.get(0).getCategoryScore())
                .isGreaterThan(result.get(1).getCategoryScore());
    }

    @Test
    void coldStartFallsBackToPopularProducts() {
        Product popular = product(2L, laptops, 10);
        Product niche   = product(3L, groceries, 10);

        // No history at all -> cold start.
        when(orderItemRepository.findPurchasedProductIds(99L, now)).thenReturn(List.of());
        when(orderItemRepository.findCategoryPurchaseCounts(99L, now)).thenReturn(List.of());
        // Popularity: product 2 sold more than product 3.
        when(orderItemRepository.findProductSalesTotals(now))
                .thenReturn(List.<Object[]>of(new Object[]{2L, 5L}, new Object[]{3L, 1L}));
        when(productRepository.findSellable()).thenReturn(List.of(popular, niche));

        List<ScoredProduct> result = service.rank(99L, now, null, 8);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(2L);  // most popular first
        assertThat(result.get(0).getExplanation()).containsIgnoringCase("popular");
    }
}
