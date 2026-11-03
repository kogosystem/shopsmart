package com.shopsmart.controller;

import com.shopsmart.service.DashboardService;
import com.shopsmart.service.EvaluationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin dashboard. Every number comes from DashboardService (real DB counts) and
 * the headline precision/recall/F1 come from the temporal EvaluationService.
 * The whole /admin/** tree is locked to ROLE_ADMIN by SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final EvaluationService evaluationService;

    public AdminDashboardController(DashboardService dashboardService,
                                    EvaluationService evaluationService) {
        this.dashboardService = dashboardService;
        this.evaluationService = evaluationService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.summary());
        model.addAttribute("productsPerCategory", dashboardService.productsPerCategory());
        model.addAttribute("feedbackBreakdown", dashboardService.feedbackBreakdown());
        // Headline accuracy of the personalised engine (top-N from properties).
        model.addAttribute("evaluation", evaluationService.evaluate(8, null));
        return "admin/dashboard";
    }
}
