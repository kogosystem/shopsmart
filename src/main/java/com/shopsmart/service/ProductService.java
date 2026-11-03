package com.shopsmart.service;

import com.shopsmart.dto.ProductForm;
import com.shopsmart.entity.Category;
import com.shopsmart.entity.Product;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Product catalogue, search, and admin CRUD. */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    /**
     * Catalogue search. The query runs in the DATABASE (not in HTML), supports
     * an optional keyword and category, and is paged + sorted. 'sort' accepts
     * "price", "name" or "newest".
     */
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, Long categoryId, String sort, int page, int size) {
        Sort sortBy = switch (sort == null ? "" : sort) {
            case "priceLow"  -> Sort.by("price").ascending();
            case "priceHigh" -> Sort.by("price").descending();
            case "name"      -> Sort.by("name").ascending();
            default           -> Sort.by("createdAt").descending();   // "newest"
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, sortBy);
        // Blank keyword becomes null so the query ignores it.
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return productRepository.search(kw, categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    /** Products that may be sold or recommended (active, in stock, active category). */
    @Transactional(readOnly = true)
    public List<Product> sellable() {
        return productRepository.findSellable();
    }

    /** A few featured products for the home page (most recent sellable ones). */
    @Transactional(readOnly = true)
    public List<Product> featured(int limit) {
        return productRepository.findSellable().stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Product> allForAdmin() {
        return productRepository.findAll(Sort.by("id").descending());
    }

    @Transactional
    public Product save(ProductForm form) {
        Product product = (form.getId() == null) ? new Product() : getById(form.getId());
        Category category = categoryService.getById(form.getCategoryId());

        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setCategory(category);
        product.setImageUrl((form.getImageUrl() == null || form.getImageUrl().isBlank())
                ? "placeholderr.png" : form.getImageUrl().trim());
        product.setActive(form.isActive());
        return productRepository.save(product);
    }

    /**
     * Safe delete: a product that appears in past orders is only DEACTIVATED,
     * so order history stays intact. A never-ordered product is hard-deleted.
     */
    @Transactional
    public void deactivateOrDelete(Long id) {
        Product product = getById(id);
        long references = productRepository.countOrderReferences(id);
        if (references > 0) {
            product.setActive(false);
            productRepository.save(product);
        } else {
            productRepository.delete(product);
        }
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return productRepository.countByActiveTrue();
    }
}
