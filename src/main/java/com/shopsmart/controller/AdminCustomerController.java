package com.shopsmart.controller;

import com.shopsmart.service.OrderService;
import com.shopsmart.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Admin view of customers and one customer's purchase history. */
@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;
    private final OrderService orderService;

    public AdminCustomerController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", userService.allCustomers());
        return "admin/customers";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("customer", userService.getById(id));
        model.addAttribute("orders", orderService.ordersForCustomer(id));
        return "admin/customer-details";
    }
}
