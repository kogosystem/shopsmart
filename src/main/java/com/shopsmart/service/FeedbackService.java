package com.shopsmart.service;

import com.shopsmart.entity.*;
import com.shopsmart.exception.BusinessException;
import com.shopsmart.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Stores customer LIKE / DISLIKE feedback and optional comments. This is the
 * input to the feedback loop: RecommendationService later reads the net
 * feedback per product and nudges a liked product's score up (or a disliked
 * one's down) on the next run.
 */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RecommendationRunRepository runRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository,
                           ProductRepository productRepository, RecommendationRunRepository runRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.runRepository = runRepository;
    }

    /**
     * Record (or update) one customer's feedback on one product. A customer can
     * change their mind, so an existing row is updated rather than duplicated.
     */
    @Transactional
    public void save(Long customerId, Long productId, FeedbackType type, String comment, Long runId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("Customer not found."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("That product does not exist."));

        Feedback feedback = feedbackRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElseGet(Feedback::new);
        feedback.setCustomer(customer);
        feedback.setProduct(product);
        feedback.setType(type);
        feedback.setComments(comment);
        if (runId != null) {
            runRepository.findById(runId).ifPresent(feedback::setRun);
        }
        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<Feedback> forCustomer(Long customerId) {
        return feedbackRepository.findByCustomerIdOrderByFeedbackDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Page<Feedback> allFeedback(int page, int size) {
        return feedbackRepository.findAllByOrderByFeedbackDateDesc(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long countLikes() { return feedbackRepository.countByType(FeedbackType.LIKE); }

    @Transactional(readOnly = true)
    public long countDislikes() { return feedbackRepository.countByType(FeedbackType.DISLIKE); }

    @Transactional(readOnly = true)
    public long countAll() { return feedbackRepository.count(); }
}
