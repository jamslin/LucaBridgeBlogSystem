package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByPostIdOrderBySortOrderAsc(Long postId);
}
