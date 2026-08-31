package com.lucabridge.core.event;

import com.lucabridge.core.publish.Visibility;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ---- public reads: every one goes through Visibility.JPQL, never status alone ----

    /**
     * "Upcoming" = hasn't ended yet (falls back to startsAt, then to always-upcoming if neither
     * is set — a TBD-dated event shouldn't vanish). Ordering is hardcoded here rather than left
     * to the caller's Pageable, since HQL's NULLS LAST isn't expressible as a Sort — the
     * controller passes an unsorted Pageable for this query on purpose.
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text WHERE "
            + Visibility.JPQL
            + "AND (COALESCE(e.endsAt, e.startsAt) IS NULL OR COALESCE(e.endsAt, e.startsAt) >= :now) "
            + "ORDER BY e.startsAt ASC NULLS LAST")
    Page<Event> findUpcomingVisible(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT e FROM Event e "
            + "LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text "
            + "LEFT JOIN FETCH e.gallery g LEFT JOIN FETCH g.media "
            + "WHERE e.slug = :slug AND " + Visibility.JPQL)
    Optional<Event> findVisibleBySlug(@Param("slug") String slug, @Param("now") Instant now);

    // ---- admin reads: every status, soft-deleted rows excluded ----

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.coverMedia WHERE e.deletedAt IS NULL")
    Page<Event> findAllActive(Pageable pageable);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    /** How many live events reference a service — used by the service-delete confirmation. */
    long countByServiceIdAndDeletedAtIsNull(Long serviceId);

    @Query("SELECT e FROM Event e "
            + "LEFT JOIN FETCH e.coverMedia LEFT JOIN FETCH e.text "
            + "LEFT JOIN FETCH e.gallery g LEFT JOIN FETCH g.media "
            + "WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Event> findActiveById(@Param("id") Long id);

    /**
     * Locks the event row for the duration of the caller's transaction, so two concurrent
     * registration submissions for the SAME event serialize: the second submission's capacity
     * count (read after the lock is granted) sees the first submission's committed insert. See
     * EventRegistrationService.submit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
