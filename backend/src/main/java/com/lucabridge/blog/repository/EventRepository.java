package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByStartsAtDesc(String status);
    Optional<Event> findBySlugAndStatus(String slug, String status);
}
