package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.CategoryDto;
import com.lucabridge.blog.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/categories")
    public List<CategoryDto> list(@RequestParam(required = false) String lang) {
        return categoryService.listCategories(lang);
    }
}
