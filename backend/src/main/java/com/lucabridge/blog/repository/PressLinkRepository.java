package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.PressLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PressLinkRepository extends JpaRepository<PressLink, Long> {
    List<PressLink> findByPostIdOrderBySortOrderAsc(Long postId);
}
