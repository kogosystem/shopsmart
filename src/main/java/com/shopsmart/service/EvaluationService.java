package com.shopsmart.service;

import com.shopsmart.entity.OrderItem;
import com.shopsmart.recommendation.EvaluationResult;
import com.shopsmart.recommendation.RecommendationService;
import com.shopsmart.recommendation.ScoredProduct;
import com.shopsmart.repository.OrderItemRepository;
import com.shopsmart.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TEMPORAL (time-split) evaluation of the recommender - the method the research
 * report will describe.
 *
 * The idea (spec section 32/68): pick a point in time (the "split"). Everything
 * BEFORE it is the training history the engine is allowed to see. Everything on
 * or AFTER it is the "ground truth" - what the customer actually went on to buy.
 * We then generate recommendations using only the past and check how many of
 * them the customer really bought afterwards.
 *
 * Because the recommender is only ever given the cut-off date (and all its
 * queries filter on orderDate < cutoff), it CANNOT see the future purchases it
 * is being judged against. That is how we avoid data leakage.
 *
 * We score two methods on the SAME split and the SAME customers:
 *   - the personalised hybrid engine
 *   - a popular-products baseline
 * so the comparison is fair.
 */
@Service
public class EvaluationService {

    private final RecommendationService recommendationService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public EvaluationService(RecommendationService recommendationService,
                             OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository) {
        this.recommendationService = recommendationService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Run the evaluation with a given top-N. If no split date is supplied we use
     * the midpoint between the first and last order so that there is history on
     * both sides of the line.
     *
     * Returns a 2-entry list: [personalised result, baseline result].
     */
    @Transactional(readOnly = true)
    public List<EvaluationResult> evaluate(int topN, LocalDateTime split) {
        LocalDateTime min = orderRepository.findMinOrderDate();
        LocalDateTime max = orderRepository.findMaxOrderDate();

        // No orders at all - nothing to evaluate yet.
        if (min == null || max == null) {
            return List.of(
                new EvaluationResult("Personalised (hybrid)", 0, 0, 0, 0, topN),
                new EvaluationResult("Popular (baseline)", 0, 0, 0, 0, topN)
            );
        }
        if (split == null) {
            long midSeconds = (max.toEpochSecond(java.time.ZoneOffset.UTC)
                             + min.toEpochSecond(java.time.ZoneOffset.UTC)) / 2;
            split = LocalDateTime.ofEpochSecond(midSeconds, 0, java.time.ZoneOffset.UTC);
        }

        long persTp = 0, persFp = 0, persFn = 0;
        long baseTp = 0, baseFp = 0, baseFn = 0;
        int evaluated = 0;

        for (Long customerId : orderRepository.findDistinctCustomerIdsWithOrders()) {

            // Ground truth = products this customer bought AFTER the split.
            Set<Long> actualFuture = new HashSet<>();
            for (OrderItem oi : orderItemRepository.findByCustomerAndWindow(
                    customerId, split, max.plusSeconds(1))) {
                actualFuture.add(oi.getProduct().getId());
            }
            // Need some training history before the split AND something bought
            // after it, otherwise this customer cannot be evaluated.
            Set<Long> pastPurchases = recommendationService.purchasedProductIds(customerId, split);
            if (pastPurchases.isEmpty() || actualFuture.isEmpty()) {
                continue;
            }
            evaluated++;

            // ---- personalised predictions (only sees data before split) ----
            Set<Long> persPredicted = toProductIds(
                    recommendationService.rank(customerId, split, null, topN));
            long tp = intersectionCount(persPredicted, actualFuture);
            persTp += tp;
            persFp += persPredicted.size() - tp;
            persFn += actualFuture.size() - tp;

            // ---- baseline predictions (same split, same top-N) ----
            Set<Long> basePredicted = toProductIds(
                    recommendationService.rankPopular(split, topN, "baseline"));
            // The baseline would also exclude already-owned items in practice; we
            // remove past purchases so both methods are judged on the same footing.
            basePredicted.removeAll(pastPurchases);
            long btp = intersectionCount(basePredicted, actualFuture);
            baseTp += btp;
            baseFp += basePredicted.size() - btp;
            baseFn += actualFuture.size() - btp;
        }

        return List.of(
            new EvaluationResult("Personalised (hybrid)", persTp, persFp, persFn, evaluated, topN),
            new EvaluationResult("Popular (baseline)", baseTp, baseFp, baseFn, evaluated, topN)
        );
    }

    private Set<Long> toProductIds(List<ScoredProduct> scored) {
        Set<Long> ids = new HashSet<>();
        for (ScoredProduct sp : scored) ids.add(sp.getProduct().getId());
        return ids;
    }

    private long intersectionCount(Set<Long> a, Set<Long> b) {
        long count = 0;
        for (Long id : a) if (b.contains(id)) count++;
        return count;
    }
}
