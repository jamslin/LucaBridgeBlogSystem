package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Post;
import com.lucabridge.blog.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndCategoryKey(PostStatus status, String categoryKey, Pageable pageable);

    Optional<Post> findBySlugAndStatus(String slug, PostStatus status);

    Optional<Post> findBySlug(String slug);

    Optional<Post> findFirstByStatusAndPublishedAtLessThanOrderByPublishedAtDesc(PostStatus status, java.time.Instant publishedAt);

    Optional<Post> findFirstByStatusAndPublishedAtGreaterThanOrderByPublishedAtAsc(PostStatus status, java.time.Instant publishedAt);
}
