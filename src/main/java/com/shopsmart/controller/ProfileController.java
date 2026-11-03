package com.shopsmart.controller;

import com.shopsmart.dto.ProfileForm;
import com.shopsmart.entity.User;
import com.shopsmart.security.ShopSmartUserPrincipal;
import com.shopsmart.service.OrderService;
import com.shopsmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** View and update the customer's own profile (safe fields only). */
@Controller
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;

    public ProfileController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal ShopSmartUserPrincipal principal, Model model) {
        User user = userService.getById(principal.getId());
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setFullName(user.getFullName());
            form.setPhone(user.getPhone());
            form.setAddress(user.getAddress());
            form.setCity(user.getCity());
            model.addAttribute("profileForm", form);
        }
        model.addAttribute("user", user);
        model.addAttribute("orderCount", orderService.ordersForCustomer(user.getId()).size());
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String update(@AuthenticationPrincipal ShopSmartUserPrincipal principal,
                         @Valid @ModelAttribute("profileForm") ProfileForm form,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("user", userService.getById(principal.getId()));
            return "customer/profile";
        }
        userService.updateProfile(principal.getId(), form);
        ra.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/profile";
    }
}
