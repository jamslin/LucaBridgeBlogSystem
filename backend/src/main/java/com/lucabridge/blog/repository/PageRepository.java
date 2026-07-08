package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findByStatusOrderBySortOrderAsc(String status);
    Optional<Page> findBySlugAndStatus(String slug, String status);
}
