package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {
    List<CategoryTranslation> findByCategoryIdIn(List<Long> categoryIds);
}
