package com.shopsmart.controller;

import com.shopsmart.service.EvaluationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin evaluation page. Runs the temporal evaluation on demand and shows the
 * personalised engine vs the popular baseline, with TP/FP/FN and the derived
 * precision, recall and F1. 'topN' is adjustable so the demonstrator can show
 * how the metrics change with the number of recommendations.
 */
@Controller
@RequestMapping("/admin/evaluation")
public class AdminEvaluationController {

    private final EvaluationService evaluationService;

    public AdminEvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public String evaluate(@RequestParam(defaultValue = "8") int topN, Model model) {
        model.addAttribute("results", evaluationService.evaluate(topN, null));
        model.addAttribute("topN", topN);
        return "admin/evaluation";
    }
}
