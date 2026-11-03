package com.shopsmart.controller;

import com.shopsmart.dto.CheckoutForm;
import com.shopsmart.entity.Cart;
import com.shopsmart.entity.Order;
import com.shopsmart.entity.User;
import com.shopsmart.service.CartService;
import com.shopsmart.service.OrderService;
import com.shopsmart.service.UserService;
import com.shopsmart.security.ShopSmartUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The checkout flow: show the form, then place the order inside a transaction. */
@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public CheckoutController(CartService cartService, OrderService orderService,
                              UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/checkout")
    public String checkoutForm(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model,
                               RedirectAttributes ra) {
        Cart cart = cartService.getCart(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Your cart is empty.");
            return "redirect:/cart";
        }
        // Pre-fill the form with the customer's saved profile details.
        User u = userService.getById(user.getId());
        CheckoutForm form = new CheckoutForm();
        form.setDeliveryName(u.getFullName());
        form.setDeliveryPhone(u.getPhone());
        form.setDeliveryAddress(u.getAddress());
        form.setDeliveryCity(u.getCity());

        model.addAttribute("checkoutForm", form);
        model.addAttribute("cart", cart);
        return "customer/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@AuthenticationPrincipal ShopSmartUserPrincipal user,
                             @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                             BindingResult binding, Model model, RedirectAttributes ra) {
        Cart cart = cartService.getCart(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Your cart is empty.");
            return "redirect:/cart";
        }
        if (binding.hasErrors()) {
            model.addAttribute("cart", cart);
            return "customer/checkout";
        }
        // The whole order is created in one transaction inside the service.
        Order order = orderService.placeOrder(userService.getById(user.getId()), form);
        return "redirect:/orders/confirmation/" + order.getId();
    }

    @GetMapping("/orders/confirmation/{id}")
    public String confirmation(@PathVariable Long id,
                               @AuthenticationPrincipal ShopSmartUserPrincipal user,
                               Model model) {
        // getOwnedBy enforces that a customer can only see their OWN order.
        model.addAttribute("order", orderService.getOwnedBy(id, user.getId()));
        return "customer/order-confirmation";
    }
}
