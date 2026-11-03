package com.shopsmart.controller;

import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** A customer's own order history and single-order view. */
@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("orders", orderService.ordersForCustomer(user.getId()));
        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String oneOrder(@PathVariable Long id,
                           @AuthenticationPrincipal ShopSmartUserPrincipal user, Model model) {
        model.addAttribute("order", orderService.getOwnedBy(id, user.getId()));
        return "customer/order-details";
    }
}
