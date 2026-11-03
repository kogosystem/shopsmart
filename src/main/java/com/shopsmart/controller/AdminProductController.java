package com.shopsmart.controller;

import com.shopsmart.dto.ProductForm;
import com.shopsmart.entity.Product;
import com.shopsmart.service.CategoryService;
import com.shopsmart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin product CRUD. Deletes are "safe" (deactivate if referenced by orders). */
@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.allForAdmin());
        return "admin/products";
    }

    @GetMapping("/new")
    public String addForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.allCategories());
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product p = productService.getById(id);
        ProductForm form = new ProductForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setDescription(p.getDescription());
        form.setPrice(p.getPrice());
        form.setStock(p.getStock());
        form.setCategoryId(p.getCategory().getId());
        form.setImageUrl(p.getImageUrl());
        form.setActive(p.isActive());
        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryService.allCategories());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("productForm") ProductForm form,
                       BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", categoryService.allCategories());
            return "admin/product-form";
        }
        productService.save(form);
        ra.addFlashAttribute("successMessage", "Product saved.");
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.deactivateOrDelete(id);
        ra.addFlashAttribute("successMessage",
                "Product removed (deactivated if it appears in past orders).");
        return "redirect:/admin/products";
    }
}
