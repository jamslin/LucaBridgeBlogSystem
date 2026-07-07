package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.CategoryDto;
import com.lucabridge.blog.entity.Category;
import com.lucabridge.blog.entity.CategoryTranslation;
import com.lucabridge.blog.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LocalizationService localizationService;

    public CategoryService(CategoryRepository categoryRepository, LocalizationService localizationService) {
        this.categoryRepository = categoryRepository;
        this.localizationService = localizationService;
    }

    public List<CategoryDto> listCategories(String lang) {
        String normalizedLang = localizationService.normalize(lang);
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> toDto(c, normalizedLang))
                .toList();
    }

    public CategoryDto toDto(Category category, String lang) {
        var resolved = localizationService.resolve(
                category.getTranslations(), lang, CategoryTranslation::getLang);

        String name = resolved.map(r -> r.value().getName()).orElse(category.getKey());
        boolean fallback = resolved.map(LocalizationService.Resolved::fallback).orElse(false);

        return new CategoryDto(category.getId(), category.getKey(), name, fallback);
    }
}
