package com.lucabridge.core.publish;

import java.time.Instant;

/**
 * The one definition of "visible to the public right now".
 *
 * <p>Visibility is decided at READ TIME, not by a scheduled job that flips a column. A cron
 * that misses a run, runs twice, or dies during a rolling update leaves content silently wrong;
 * a read-time predicate cannot drift, needs no scheduler, and is correct the instant a pod
 * comes back up. The homepage banners already worked this way and never had a scheduling bug.
 *
 * <p>Boundaries: {@code publishAt} is inclusive (a record set to go live at 09:00 is live at
 * 09:00) and {@code unpublishAt} is exclusive (a record set to close at 17:00 is gone at 17:00).
 *
 * <p>{@link #JPQL} is the canonical predicate. Every public repository query must use it. The
 * bug it exists to prevent is the old codebase's, where eight separate queries filtered on
 * status alone — which is why expired job postings stayed on the careers page forever.
 */
public final class Visibility {

    private Visibility() {
    }

    /**
     * Canonical predicate for blog, event and job queries. The entity alias must be {@code e}
     * and the query must bind {@code :now}.
     */
    public static final String JPQL = """
            e.status = com.lucabridge.core.publish.PublishStatus.PUBLISHED
            and e.deletedAt is null
            and (e.publishAt is null or e.publishAt <= :now)
            and (e.unpublishAt is null or e.unpublishAt > :now)
            """;

    /**
     * Job variant. Adds the application deadline, so a role past {@code closesAt} drops off the
     * careers page without anyone unpublishing it by hand.
     */
    public static final String JPQL_JOB = JPQL
            + "and (e.closesAt is null or e.closesAt > :now)\n";

    /**
     * home_block variant — that table has no {@code status} enum or soft delete, just
     * {@code is_active} plus the same publish window, so {@link #JPQL} doesn't fit it directly.
     * The entity alias must be {@code e}, the boolean field must be named {@code active}, and
     * the query must bind {@code :now}.
     */
    public static final String JPQL_ACTIVE = """
            e.active = true
            and (e.publishAt is null or e.publishAt <= :now)
            and (e.unpublishAt is null or e.unpublishAt > :now)
            """;

    /** In-memory equivalent of {@link #JPQL}, for tests and for already-loaded entities. */
    public static boolean isVisible(PublishStatus status,
                                    Instant publishAt,
                                    Instant unpublishAt,
                                    Instant deletedAt,
                                    Instant now) {
        if (status != PublishStatus.PUBLISHED || deletedAt != null) {
            return false;
        }
        if (publishAt != null && publishAt.isAfter(now)) {
            return false;
        }
        return unpublishAt == null || unpublishAt.isAfter(now);
    }

    /** In-memory equivalent of {@link #JPQL_JOB}. */
    public static boolean isJobVisible(PublishStatus status,
                                       Instant publishAt,
                                       Instant unpublishAt,
                                       Instant closesAt,
                                       Instant deletedAt,
                                       Instant now) {
        return isVisible(status, publishAt, unpublishAt, deletedAt, now)
                && (closesAt == null || closesAt.isAfter(now));
    }

    /** In-memory equivalent of {@link #JPQL_ACTIVE}. */
    public static boolean isActiveVisible(boolean active, Instant publishAt, Instant unpublishAt, Instant now) {
        if (!active) {
            return false;
        }
        if (publishAt != null && publishAt.isAfter(now)) {
            return false;
        }
        return unpublishAt == null || unpublishAt.isAfter(now);
    }

    /**
     * How a record should be labelled in the CMS list. The client must never see "Published"
     * next to something that is not actually on the site.
     */
    public static State stateOf(PublishStatus status,
                                Instant publishAt,
                                Instant unpublishAt,
                                Instant deletedAt,
                                Instant now) {
        if (deletedAt != null) {
            return State.DELETED;
        }
        if (status == PublishStatus.ARCHIVED) {
            return State.ARCHIVED;
        }
        if (status == PublishStatus.DRAFT) {
            return State.DRAFT;
        }
        if (publishAt != null && publishAt.isAfter(now)) {
            return State.SCHEDULED;
        }
        if (unpublishAt != null && !unpublishAt.isAfter(now)) {
            return State.EXPIRED;
        }
        return State.LIVE;
    }

    /**
     * Job variant of {@link #stateOf}. A role past {@code closesAt} is EXPIRED even when its
     * publish window is still open — {@link #JPQL_JOB} has already dropped it from the public
     * query, so the CMS list must not still say LIVE. LIVE is the only state closesAt can
     * downgrade: a DRAFT, SCHEDULED, ARCHIVED or DELETED role isn't visible either way.
     */
    public static State stateOfJob(PublishStatus status,
                                   Instant publishAt,
                                   Instant unpublishAt,
                                   Instant closesAt,
                                   Instant deletedAt,
                                   Instant now) {
        State state = stateOf(status, publishAt, unpublishAt, deletedAt, now);
        if (state == State.LIVE && closesAt != null && !closesAt.isAfter(now)) {
            return State.EXPIRED;
        }
        return state;
    }

    /** What the CMS badge says. */
    public enum State {
        DRAFT, SCHEDULED, LIVE, EXPIRED, ARCHIVED, DELETED
    }
}
