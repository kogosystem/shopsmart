package com.shopsmart.service;

import com.shopsmart.dto.CategoryForm;
import com.shopsmart.entity.Category;
import com.shopsmart.exception.BusinessException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CategoryRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Category CRUD, including the "safe deactivate" rule. */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> activeCategories() {
        return categoryRepository.findByActiveTrueOrderByName();
    }

    @Transactional(readOnly = true)
    public List<Category> allCategories() {
        return categoryRepository.findAllByOrderByName();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category save(CategoryForm form) {
        Category category = (form.getId() == null) ? new Category() : getById(form.getId());

        // Block duplicate names (but allow a category to keep its own name on edit).
        categoryRepository.findByName(form.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(form.getId())) {
                throw new BusinessException("A category with that name already exists.");
            }
        });

        category.setName(form.getName());
        category.setDescription(form.getDescription());
        category.setActive(form.isActive());
        return categoryRepository.save(category);
    }

    /**
     * "Safe delete" (spec section 103): if products still belong to this
     * category we only DEACTIVATE it, so historical orders and products are
     * never orphaned. Only a truly empty category is hard-deleted.
     */
    @Transactional
    public void deactivateOrDelete(Long id) {
        Category category = getById(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            category.setActive(false);
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }
    }

    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}
