package com.shopsmart.controller;

import com.shopsmart.dto.CategoryForm;
import com.shopsmart.entity.Category;
import com.shopsmart.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin category CRUD with safe deactivate. */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.allCategories());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        return "admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category c = categoryService.getById(id);
        CategoryForm form = new CategoryForm();
        form.setId(c.getId());
        form.setName(c.getName());
        form.setDescription(c.getDescription());
        form.setActive(c.isActive());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categories", categoryService.allCategories());
        return "admin/categories";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                       BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", categoryService.allCategories());
            return "admin/categories";
        }
        categoryService.save(form);
        ra.addFlashAttribute("successMessage", "Category saved.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deactivateOrDelete(id);
        ra.addFlashAttribute("successMessage",
                "Category removed (deactivated if it still has products).");
        return "redirect:/admin/categories";
    }
}
