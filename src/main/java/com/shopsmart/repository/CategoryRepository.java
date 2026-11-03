package com.shopsmart.repository;

import com.shopsmart.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    /** Only active categories, for the customer-facing navigation. */
    List<Category> findByActiveTrueOrderByName();

    /** All categories including inactive ones, for the admin screen. */
    List<Category> findAllByOrderByName();
}
