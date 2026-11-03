package com.shopsmart.controller;

import com.shopsmart.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Admin view of all customer feedback, with like/dislike totals. */
@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("feedbackPage", feedbackService.allFeedback(page, 20));
        model.addAttribute("likes", feedbackService.countLikes());
        model.addAttribute("dislikes", feedbackService.countDislikes());
        return "admin/feedback";
    }
}
