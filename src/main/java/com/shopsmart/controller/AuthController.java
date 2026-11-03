package com.shopsmart.controller;

import com.shopsmart.dto.RegisterForm;
import com.shopsmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Login page (Spring Security handles the POST) and customer registration. */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** Just shows the login form. Spring Security processes the submitted login. */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    /**
     * Create the account. @Valid triggers the Bean Validation rules on the form;
     * if anything fails we re-show the page with the messages. The service does
     * the password-match and duplicate-email checks and hashes the password.
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult binding, RedirectAttributes ra) {
        if (!form.passwordsMatch()) {
            binding.rejectValue("confirmPassword", "mismatch", "The two passwords do not match.");
        }
        if (binding.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form);
        } catch (com.shopsmart.exception.BusinessException ex) {
            binding.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/register";
        }
        ra.addFlashAttribute("successMessage", "Account created. Please log in.");
        return "redirect:/login";
    }
}
