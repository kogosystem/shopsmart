package com.shopsmart.dto;

import jakarta.validation.constraints.*;

/** Admin add/edit category form. */
public class CategoryForm {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 80)
    private String name;

    @Size(max = 255)
    private String description;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
