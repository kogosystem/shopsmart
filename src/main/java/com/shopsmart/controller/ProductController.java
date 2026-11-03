package com.shopsmart.controller;

import com.shopsmart.entity.Product;
import com.shopsmart.recommendation.RecommendationService;
import com.shopsmart.recommendation.ScoredProduct;
import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.CategoryService;
import com.shopsmart.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/** Public product catalogue: list, search, filter, sort, details. */
@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final RecommendationService recommendationService;

    public ProductController(ProductService productService, CategoryService categoryService,
                             RecommendationService recommendationService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.recommendationService = recommendationService;
    }

    /**
     * Catalogue page. All of keyword, category, sort and page are optional query
     * parameters, so /products, /products?keyword=phone and
     * /products?categoryId=2&sort=priceLow all work. The search runs in the DB.
     */
    @GetMapping("/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false, defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Product> products = productService.search(keyword, categoryId, sort, page, 12);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.activeCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("sort", sort);
        return "customer/products";
    }

    /** Convenience URL: /category/3 shows that category's products. */
    @GetMapping("/category/{id}")
    public String byCategory(@PathVariable Long id, Model model) {
        return list(null, id, "newest", 0, model);
    }

    /**
     * Product details page, including a "You may also like" block. For a logged-in
     * customer this uses the personalised engine with the current product's
     * category as CONTEXT; for a guest it falls back to popular products.
     */
    @GetMapping("/products/{id}")
    public String details(@PathVariable Long id,
                          @AuthenticationPrincipal ShopSmartUserPrincipal user,
                          Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);

        List<ScoredProduct> related;
        if (user != null) {
            related = recommendationService.rank(user.getId(), LocalDateTime.now(),
                    product.getCategory().getId(), 4);
        } else {
            related = recommendationService.rankPopular(LocalDateTime.now(), 4, "Popular product");
        }
        // Never show the product itself in its own "related" list.
        related = related.stream()
                .filter(sp -> !sp.getProduct().getId().equals(id))
                .limit(4).toList();
        model.addAttribute("related", related);
        return "customer/product-details";
    }
}
