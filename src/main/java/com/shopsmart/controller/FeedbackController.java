package com.shopsmart.controller;

import com.shopsmart.entity.FeedbackType;
import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.FeedbackService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Customer feedback. The Like / Dislike buttons on recommendation cards POST
 * here; the feedback is saved and then used by the engine on the next run.
 */
@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /** The customer's own feedback history page. */
    @GetMapping("/feedback")
    public String myFeedback(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("feedbackList", feedbackService.forCustomer(user.getId()));
        return "customer/feedback";
    }

    /**
     * Save a LIKE or DISLIKE (with an optional comment and the run it came from).
     * After saving we return the customer to where they clicked.
     */
    @PostMapping("/feedback/submit")
    public String submit(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                         @RequestParam Long productId,
                         @RequestParam FeedbackType type,
                         @RequestParam(required = false) String comment,
                         @RequestParam(required = false) Long runId,
                         @RequestParam(required = false, defaultValue = "/recommendations") String redirect,
                         RedirectAttributes ra) {
        feedbackService.save(user.getId(), productId, type, comment, runId);
        ra.addFlashAttribute("successMessage",
                "Thanks! Your " + type.name().toLowerCase() + " was saved and will shape your next picks.");
        return "redirect:" + redirect;
    }
}
