package com.lucabridge.core.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.iconMedia LEFT JOIN FETCH s.text "
            + "WHERE s.active = true ORDER BY s.sortOrder ASC")
    List<Service> findAllActive();

    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.iconMedia LEFT JOIN FETCH s.text ORDER BY s.sortOrder ASC")
    List<Service> findAllForAdmin();

    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.iconMedia LEFT JOIN FETCH s.text WHERE s.id = :id")
    Optional<Service> findByIdWithText(@Param("id") Long id);
}
