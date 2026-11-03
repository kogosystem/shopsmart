package com.shopsmart.controller;

import com.shopsmart.recommendation.RecommendationService;
import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.CategoryService;
import com.shopsmart.service.OrderService;
import com.shopsmart.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** The public landing page and the logged-in customer home page. */
@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final RecommendationService recommendationService;

    public HomeController(ProductService productService, CategoryService categoryService,
                          OrderService orderService, RecommendationService recommendationService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.recommendationService = recommendationService;
    }

    /** Public landing page. Logged-in customers are sent to their personal home. */
    @GetMapping("/")
    public String index(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        if (user != null) {
            return "redirect:/home";
        }
        model.addAttribute("categories", categoryService.activeCategories());
        model.addAttribute("featured", productService.featured(8));
        return "index";
    }

    /** Logged-in customer home: welcome, recommendations, recent orders, categories. */
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("categories", categoryService.activeCategories());
        model.addAttribute("featured", productService.featured(8));
        model.addAttribute("recentOrders", orderService.recentForCustomer(user.getId(), 3));
        // The personalised block: reuse the latest run, or generate the first one.
        model.addAttribute("run", recommendationService.latestOrGenerate(user.getId(), null));
        return "customer/home";
    }
}
