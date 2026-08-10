package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatusOrderByPostedAtDesc(String status);
    Optional<JobPosting> findBySlugAndStatus(String slug, String status);
}
