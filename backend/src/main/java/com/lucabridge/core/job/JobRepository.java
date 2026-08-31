package com.lucabridge.core.job;

import com.lucabridge.core.publish.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    // ---- public reads: JPQL_JOB, not JPQL — an expired posting must drop off without anyone unpublishing it ----

    @Query("SELECT e FROM Job e LEFT JOIN FETCH e.text WHERE " + Visibility.JPQL_JOB)
    Page<Job> findVisible(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT e FROM Job e LEFT JOIN FETCH e.text WHERE e.slug = :slug AND " + Visibility.JPQL_JOB)
    Optional<Job> findVisibleBySlug(@Param("slug") String slug, @Param("now") Instant now);

    // ---- admin reads: every status, soft-deleted rows excluded ----

    @Query("SELECT e FROM Job e WHERE e.deletedAt IS NULL")
    Page<Job> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Job e LEFT JOIN FETCH e.text WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Job> findActiveById(@Param("id") Long id);
}
