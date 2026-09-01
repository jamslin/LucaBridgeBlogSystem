package com.lucabridge.core.homeblock;

import com.lucabridge.core.publish.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HomeBlockRepository extends JpaRepository<HomeBlock, Long> {

    /** All slots in one call — Visibility.JPQL_ACTIVE, not JPQL: home_block has no status/deletedAt. */
    @Query("SELECT e FROM HomeBlock e LEFT JOIN FETCH e.media LEFT JOIN FETCH e.text LEFT JOIN FETCH e.blog "
            + "WHERE " + Visibility.JPQL_ACTIVE
            + "ORDER BY e.slot ASC, e.sortOrder ASC")
    List<HomeBlock> findAllVisible(@Param("now") Instant now);

    @Query("SELECT e FROM HomeBlock e LEFT JOIN FETCH e.media LEFT JOIN FETCH e.text LEFT JOIN FETCH e.blog "
            + "ORDER BY e.slot ASC, e.sortOrder ASC")
    List<HomeBlock> findAllForAdmin();

    /** Used to enforce the one-per-slot rule on HERO, SUPPORT and FEATURED. */
    long countBySlot(HomeBlockSlot slot);

    @Query("SELECT e FROM HomeBlock e LEFT JOIN FETCH e.media LEFT JOIN FETCH e.text LEFT JOIN FETCH e.blog "
            + "WHERE e.id = :id")
    Optional<HomeBlock> findByIdWithText(@Param("id") Long id);
}
