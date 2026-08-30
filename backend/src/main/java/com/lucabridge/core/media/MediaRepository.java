package com.lucabridge.core.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Reference counting and unused-media lookup delegate to the {@code media_usage} and
 * {@code media_unused} database views rather than reimplementing them in Java — the views are
 * already verified against a real Postgres, including the markdown-body exclusion.
 */
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query(value = "SELECT count(*) FROM media_usage WHERE media_id = :mediaId", nativeQuery = true)
    long countUsage(@Param("mediaId") Long mediaId);

    /** What the sweeper's DB-row pass may delete. Columns mirror {@link Media} exactly. */
    @Query(value = "SELECT * FROM media_unused", nativeQuery = true)
    List<Media> findUnused();

    /** Projection for building the catalogued-keys set — avoids loading every full Media row. */
    @Query("SELECT m.s3Key FROM Media m")
    List<String> findAllS3Keys();
}
