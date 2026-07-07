package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.PostTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostTranslationRepository extends JpaRepository<PostTranslation, Long> {
    List<PostTranslation> findByPostIdIn(List<Long> postIds);
    Optional<PostTranslation> findByPostIdAndLang(Long postId, String lang);
}
