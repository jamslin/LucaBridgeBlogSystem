package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
