package com.lucabridge.core.blog;

import com.lucabridge.core.publish.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    // ---- public reads: every one goes through Visibility.JPQL, never status alone ----

    @Query("SELECT e FROM Blog e LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text WHERE " + Visibility.JPQL)
    Page<Blog> findVisible(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT e FROM Blog e "
            + "LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text "
            + "LEFT JOIN FETCH e.gallery g LEFT JOIN FETCH g.media "
            + "WHERE e.slug = :slug AND " + Visibility.JPQL)
    Optional<Blog> findVisibleBySlug(@Param("slug") String slug, @Param("now") Instant now);

    // ---- admin reads: every status, soft-deleted rows excluded ----

    @Query("SELECT e FROM Blog e LEFT JOIN FETCH e.coverMedia WHERE e.deletedAt IS NULL")
    Page<Blog> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Blog e "
            + "LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text "
            + "LEFT JOIN FETCH e.gallery g LEFT JOIN FETCH g.media "
            + "WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Blog> findActiveById(@Param("id") Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    /** How many live posts reference a service — used by the service-delete confirmation. */
    long countByServiceIdAndDeletedAtIsNull(Long serviceId);
}
