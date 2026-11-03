package com.shopsmart.config;

import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.CartService;
import com.shopsmart.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Puts a few values into the model on EVERY page so the shared navbar fragment
 * can show the logged-in user's name, whether they are an admin, the cart-count
 * badge, and the category menu - without every controller adding them by hand.
 *
 * This replaces the Thymeleaf-Security dialect we deliberately did not add, and
 * keeps the templates using plain, easy-to-read th:if checks.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final CartService cartService;
    private final CategoryService categoryService;

    public GlobalModelAdvice(CartService cartService, CategoryService categoryService) {
        this.cartService = cartService;
        this.categoryService = categoryService;
    }

    /** Categories shown in the navbar dropdown on every page. */
    @ModelAttribute("navCategories")
    public java.util.List<com.shopsmart.entity.Category> navCategories() {
        return categoryService.activeCategories();
    }

    @ModelAttribute("currentUser")
    public ShopSmartUserPrincipal currentUser() {
        return principal();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /** The number shown on the cart icon. 0 when not logged in. */
    @ModelAttribute("cartCount")
    public int cartCount() {
        ShopSmartUserPrincipal p = principal();
        if (p == null) return 0;
        return cartService.itemCount(p.getId());
    }

    private ShopSmartUserPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof ShopSmartUserPrincipal p) {
            return p;
        }
        return null;
    }
}
