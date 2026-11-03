package com.shopsmart.controller;

import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.CartService;
import com.shopsmart.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The shopping cart. All rules (stock, quantity, ownership) live in CartService. */
@Controller
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("cart", cartService.getCart(user.getId()));
        return "customer/cart";
    }

    @PostMapping("/cart/add")
    public String add(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                      @RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      RedirectAttributes ra) {
        cartService.addToCart(userService.getById(user.getId()), productId, quantity);
        ra.addFlashAttribute("successMessage", "Added to your cart.");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/cart/update")
    public String update(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                         @RequestParam Long cartItemId,
                         @RequestParam int quantity,
                         RedirectAttributes ra) {
        cartService.updateQuantity(user.getId(), cartItemId, quantity);
        ra.addFlashAttribute("successMessage", "Cart updated.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                         @RequestParam Long cartItemId,
                         RedirectAttributes ra) {
        cartService.removeItem(user.getId(), cartItemId);
        ra.addFlashAttribute("successMessage", "Item removed.");
        return "redirect:/cart";
    }
}
