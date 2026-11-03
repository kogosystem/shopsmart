package com.shopsmart.controller;

import com.shopsmart.entity.OrderStatus;
import com.shopsmart.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin order management: list all orders and update their status. */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("orders", orderService.allOrders(page, 15));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes ra) {
        orderService.updateStatus(id, status);
        ra.addFlashAttribute("successMessage", "Order #" + id + " set to " + status + ".");
        return "redirect:/admin/orders";
    }
}
