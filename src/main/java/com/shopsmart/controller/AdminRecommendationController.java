package com.shopsmart.controller;

import com.shopsmart.recommendation.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin recommendation log. Lists every run and lets the admin open one to see
 * the ranked products WITH their component scores - the transparency view that
 * makes the algorithm easy to demonstrate and defend.
 */
@Controller
@RequestMapping("/admin/recommendations")
public class AdminRecommendationController {

    private final RecommendationService recommendationService;

    public AdminRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("runs", recommendationService.recentRuns(page, 15));
        return "admin/recommendations";
    }

    @GetMapping("/{runId}")
    public String runDetails(@PathVariable Long runId, Model model) {
        model.addAttribute("run", recommendationService.getRun(runId));
        return "admin/recommendation-details";
    }
}
