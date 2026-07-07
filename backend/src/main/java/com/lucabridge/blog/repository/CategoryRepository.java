package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderBySortOrderAsc();
    Optional<Category> findByKey(String key);
}
