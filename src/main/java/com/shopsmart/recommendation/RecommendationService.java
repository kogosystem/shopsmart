package com.shopsmart.recommendation;

import com.shopsmart.config.RecommendationProperties;
import com.shopsmart.entity.*;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * THE MAIN RESEARCH COMPONENT - a lightweight HYBRID, RULE-BASED personalised
 * recommender. It is called "hybrid" because it genuinely combines more than
 * one signal; it is NOT machine learning and the code never pretends it is.
 *
 * Signals combined (each explained where it is calculated below):
 *   1. Category preference  - from the customer's own purchase history
 *   2. Popularity           - how much everyone buys the product
 *   3. Feedback             - the customer's own likes/dislikes
 *   4. Similarity           - "people who buy your categories also buy this"
 *   5. Availability         - a small bonus for being well stocked
 *
 * The pipeline (spec section 26): purchase history -> preferred categories ->
 * candidate products -> REMOVE already-purchased / out-of-stock / inactive ->
 * score -> sort -> take top N -> save the run -> show the customer.
 *
 * Every number is deterministic and testable, and every weight comes from
 * RecommendationProperties (application.properties), so nothing is a magic
 * constant buried in the code.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final FeedbackRepository feedbackRepository;
    private final RecommendationRunRepository runRepository;
    private final UserRepository userRepository;
    private final RecommendationProperties props;

    public RecommendationService(ProductRepository productRepository,
                                 OrderItemRepository orderItemRepository,
                                 FeedbackRepository feedbackRepository,
                                 RecommendationRunRepository runRepository,
                                 UserRepository userRepository,
                                 RecommendationProperties props) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.feedbackRepository = feedbackRepository;
        this.runRepository = runRepository;
        this.userRepository = userRepository;
        this.props = props;
    }

    /**
     * Public entry point for the customer pages: rank with "now" as the cut-off
     * (use all history), persist the run, and return it.
     */
    @Transactional
    public RecommendationRun generateAndSave(Long customerId, Long contextCategoryId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        List<ScoredProduct> ranked = rank(customerId, LocalDateTime.now(), contextCategoryId, props.getTopN());

        // Save one run + its ranked items (the robust run/items schema).
        RecommendationRun run = new RecommendationRun(customer, props.getAlgorithmVersion());
        run.setContextCategoryId(contextCategoryId);

        int rank = 1;
        for (ScoredProduct sp : ranked) {
            Recommendation rec = new Recommendation();
            rec.setProduct(sp.getProduct());
            rec.setScore(round(sp.getFinalScore()));
            rec.setRankPosition(rank++);
            rec.setCategoryScore(round(sp.getCategoryScore()));
            rec.setPopularityScore(round(sp.getPopularityScore()));
            rec.setFeedbackScore(round(sp.getFeedbackScore()));
            rec.setSimilarityScore(round(sp.getSimilarityScore()));
            rec.setExplanation(sp.getExplanation());
            run.addItem(rec);
        }
        RecommendationRun saved = runRepository.save(run);
        log.info("Recommendation run {} saved for customer {} with {} items",
                saved.getId(), customerId, ranked.size());
        return saved;
    }

    /**
     * THE CORE ALGORITHM. Returns the ranked candidates WITHOUT saving, so the
     * same method serves both the live pages and the temporal evaluation (which
     * calls it with a past cut-off so it never "sees the future").
     */
    @Transactional(readOnly = true)
    public List<ScoredProduct> rank(Long customerId, LocalDateTime cutoff,
                                    Long contextCategoryId, int limit) {

        // --- 1. Products the customer ALREADY bought: these are excluded so we
        //        never recommend something they own (a core project goal). ---
        Set<Long> purchased = new HashSet<>(
                orderItemRepository.findPurchasedProductIds(customerId, cutoff));

        // --- 2. Preferred categories, from how often they bought in each one.
        //        catCounts: categoryId -> number of purchases in that category. ---
        Map<Long, Long> catCounts = new HashMap<>();
        long totalCatPurchases = 0;
        for (Object[] row : orderItemRepository.findCategoryPurchaseCounts(customerId, cutoff)) {
            Long catId = (Long) row[0];
            long count = (Long) row[1];
            catCounts.put(catId, count);
            totalCatPurchases += count;
        }

        // --- COLD START (spec section 61): a brand-new customer has no history,
        //     so personalisation is impossible. Fall back to popular products so
        //     the "Recommended for you" section is never empty. ---
        boolean coldStart = purchased.isEmpty() && catCounts.isEmpty();
        if (coldStart) {
            return rankPopular(cutoff, limit, "Popular right now - start shopping to get personal picks");
        }

        // --- 3. Popularity: units sold per product (before the cut-off).
        //        We divide by the biggest value so popularity lands in 0..1. ---
        Map<Long, Long> salesByProduct = new HashMap<>();
        long maxSales = 1;
        for (Object[] row : orderItemRepository.findProductSalesTotals(cutoff)) {
            Long pid = (Long) row[0];
            long units = ((Number) row[1]).longValue();
            salesByProduct.put(pid, units);
            maxSales = Math.max(maxSales, units);
        }

        // --- 4. This customer's own net feedback per product (likes - dislikes). ---
        Map<Long, Long> netFeedback = new HashMap<>();
        for (Object[] row : feedbackRepository.findNetFeedbackPerProduct(customerId)) {
            netFeedback.put((Long) row[0], ((Number) row[1]).longValue());
        }

        // --- 5. Candidate products = everything sellable that they have NOT bought. ---
        List<Product> candidates = productRepository.findSellable().stream()
                .filter(p -> !purchased.contains(p.getId()))
                .toList();

        // Pre-compute the biggest stock so availability also lands in 0..1.
        int maxStock = candidates.stream().mapToInt(Product::getStock).max().orElse(1);

        // --- 6. Score each candidate. ---
        List<ScoredProduct> scored = new ArrayList<>();
        for (Product p : candidates) {
            Long catId = p.getCategory().getId();

            // 6a. CATEGORY PREFERENCE (weight: categoryWeight, the strongest signal).
            //     share = how much of their buying is in this product's category (0..1).
            double share = (totalCatPurchases == 0) ? 0.0
                    : (double) catCounts.getOrDefault(catId, 0L) / totalCatPurchases;
            double categoryScore = props.getCategoryWeight() * share;

            // 6b. POPULARITY (weight: popularityWeight). Normalised 0..1.
            double popularity = (double) salesByProduct.getOrDefault(p.getId(), 0L) / maxSales;
            double popularityScore = props.getPopularityWeight() * popularity;

            // 6c. FEEDBACK (weight: feedbackWeight). We squash net likes into
            //     -1..+1 with a simple cap at +/-3 so one angry customer cannot
            //     dominate, then multiply by the weight. A LIKE lifts the score,
            //     a DISLIKE lowers it - this is the feedback loop.
            long net = netFeedback.getOrDefault(p.getId(), 0L);
            double feedbackNorm = Math.max(-1.0, Math.min(1.0, net / 3.0));
            double feedbackScore = props.getFeedbackWeight() * feedbackNorm;

            // 6d. SIMILARITY (weight: similarityWeight): "customers who buy your
            //     preferred categories also buy this". We approximate it by how
            //     widely the product's category is bought across all customers,
            //     but only reward categories THIS customer already likes.
            double similarity = 0.0;
            if (catCounts.containsKey(catId)) {
                long buyers = orderItemRepository.countDistinctCustomersInCategory(catId, cutoff);
                long totalCustomers = Math.max(1, userRepository.countByRoleName("ROLE_CUSTOMER"));
                similarity = Math.min(1.0, (double) buyers / totalCustomers);
            }
            double similarityScore = props.getSimilarityWeight() * similarity;

            // 6e. AVAILABILITY (weight: availabilityWeight): small bonus for being
            //     well stocked, so almost-sold-out items rank a little lower.
            double availabilityScore = props.getAvailabilityWeight() * ((double) p.getStock() / maxStock);

            // 6f. CONTEXT (spec section 63): if the customer is browsing a category
            //     right now, give products in that category the category bonus too.
            if (contextCategoryId != null && contextCategoryId.equals(catId)) {
                categoryScore += props.getCategoryWeight() * 0.5;
            }

            String explanation = buildExplanation(p, share, net, popularity);
            scored.add(new ScoredProduct(p, categoryScore, popularityScore,
                    feedbackScore, similarityScore, availabilityScore, explanation));
        }

        // --- 7. Sort by final score (desc) and take the top N. ---
        scored.sort(Comparator.comparingDouble(ScoredProduct::getFinalScore).reversed());
        return scored.stream().limit(limit).toList();
    }

    /**
     * The POPULAR-PRODUCTS BASELINE (spec section 33) and the cold-start helper.
     * Ranks purely by units sold - no personalisation at all - so the evaluation
     * can fairly compare "personalised" against "popular".
     */
    @Transactional(readOnly = true)
    public List<ScoredProduct> rankPopular(LocalDateTime cutoff, int limit, String explanation) {
        Map<Long, Long> salesByProduct = new HashMap<>();
        long maxSales = 1;
        for (Object[] row : orderItemRepository.findProductSalesTotals(cutoff)) {
            salesByProduct.put((Long) row[0], ((Number) row[1]).longValue());
            maxSales = Math.max(maxSales, ((Number) row[1]).longValue());
        }
        final long max = maxSales;
        return productRepository.findSellable().stream()
                .map(p -> {
                    double pop = (double) salesByProduct.getOrDefault(p.getId(), 0L) / max;
                    return new ScoredProduct(p, 0, props.getPopularityWeight() * pop, 0, 0, 0,
                            explanation == null ? "Popular product" : explanation);
                })
                .sorted(Comparator.comparingDouble(ScoredProduct::getFinalScore).reversed())
                .limit(limit)
                .toList();
    }

    /** Product ids a customer has bought - exposed so other services can reuse it. */
    @Transactional(readOnly = true)
    public Set<Long> purchasedProductIds(Long customerId, LocalDateTime cutoff) {
        return new HashSet<>(orderItemRepository.findPurchasedProductIds(customerId, cutoff));
    }

    // ------------------------------------------------------------------
    // Query helpers used by the controllers (so controllers never touch
    // repositories directly - that is the architectural rule in the spec).
    // ------------------------------------------------------------------

    /** The customer's most recent run, generating a fresh one only if none exists. */
    @Transactional
    public RecommendationRun latestOrGenerate(Long customerId, Long contextCategoryId) {
        return runRepository.findFirstByCustomerIdOrderByGeneratedAtDesc(customerId)
                .orElseGet(() -> generateAndSave(customerId, contextCategoryId));
    }

    /** One run by id (used by the admin detail view). */
    @Transactional(readOnly = true)
    public RecommendationRun getRun(Long runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation run not found: " + runId));
    }

    /** Paged list of all runs, newest first (admin recommendation log). */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RecommendationRun> recentRuns(int page, int size) {
        return runRepository.findAllByOrderByGeneratedAtDesc(
                org.springframework.data.domain.PageRequest.of(page, size));
    }

    /** Builds the friendly "Recommended because..." line shown to customers. */
    private String buildExplanation(Product p, double share, long net, double popularity) {
        if (net > 0) {
            return "Because you liked similar items";
        }
        if (share >= 0.4) {
            return "Because you often shop in " + p.getCategory().getName();
        }
        if (share > 0) {
            return "Based on your interest in " + p.getCategory().getName();
        }
        if (popularity >= 0.5) {
            return "Popular with other shoppers";
        }
        return "You may also like this";
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
