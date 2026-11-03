package com.shopsmart.controller;

import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.recommendation.RecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The customer "Recommended for you" page and the manual refresh button. */
@Controller
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /** Show the latest run (or generate the first one if none exists yet). */
    @GetMapping("/recommendations")
    public String view(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("run", recommendationService.latestOrGenerate(user.getId(), null));
        return "customer/recommendations";
    }

    /** "Refresh my recommendations" - runs the engine again and logs a new run. */
    @PostMapping("/recommendations/refresh")
    public String refresh(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                          RedirectAttributes ra) {
        recommendationService.generateAndSave(user.getId(), null);
        ra.addFlashAttribute("successMessage", "Your recommendations have been refreshed.");
        return "redirect:/recommendations";
    }
}
