package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.Category;
import com.lucabridge.blog.entity.CategoryTranslation;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ConflictException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.CategoryRepository;
import com.lucabridge.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Admin CRUD for Categories (taxonomy: key + sort order + a name per language). */
@Service
public class AdminCategoryService {

    private final CategoryRepository repo;
    private final PostRepository postRepository;

    public AdminCategoryService(CategoryRepository repo, PostRepository postRepository) {
        this.repo = repo;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryDto> list() {
        return repo.findAllByOrderBySortOrderAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public Long upsert(CategoryUpsertRequest req) {
        Category c;
        if (req.id() != null) {
            c = repo.findById(req.id()).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.id()));
            String key = req.key().trim();
            if (!c.getKey().equals(key) && postRepository.countByCategoryKey(c.getKey()) > 0) {
                throw new ConflictException("Category key cannot be changed while posts use it");
            }
            if (!c.getKey().equals(key) && repo.findByKey(key).isPresent()) {
                throw new ConflictException("Category key already exists: " + key);
            }
        } else {
            if (repo.findByKey(req.key().trim()).isPresent()) {
                throw new ConflictException("Category key already exists: " + req.key());
            }
            c = Category.builder().build();
        }
        c.setKey(req.key().trim());
        c.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);

        Map<String, CategoryTranslation> existing = new HashMap<>();
        for (CategoryTranslation t : c.getTranslations()) existing.put(t.getLang(), t);
        Set<String> langs = new HashSet<>();
        for (CategoryTranslationInput in : req.translations()) {
            if (in.name() == null || in.name().isBlank()) continue;
            langs.add(in.lang());
            CategoryTranslation t = existing.get(in.lang());
            if (t == null) { t = CategoryTranslation.builder().category(c).lang(in.lang()).build(); c.getTranslations().add(t); }
            t.setName(in.name());
        }
        if (langs.isEmpty()) throw new BadRequestException("At least one name is required");
        c.getTranslations().removeIf(t -> !langs.contains(t.getLang()));
        return repo.save(c).getId();
    }

    @Transactional
    public void delete(Long id) {
        Category c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        long posts = postRepository.countByCategoryKey(c.getKey());
        if (posts > 0) {
            throw new BadRequestException("Cannot delete — " + posts + " post(s) use this category. Reassign them first.");
        }
        repo.delete(c);
    }

    private AdminCategoryDto toDto(Category c) {
        List<CategoryTranslationInput> tr = c.getTranslations().stream()
                .map(t -> new CategoryTranslationInput(t.getLang(), t.getName()))
                .toList();
        return new AdminCategoryDto(c.getId(), c.getKey(), c.getSortOrder(), tr, postRepository.countByCategoryKey(c.getKey()));
    }
}
