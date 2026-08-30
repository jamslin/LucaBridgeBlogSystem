package com.lucabridge.core.publish;

/**
 * Manual publication state. Independent of the scheduled window: a DRAFT is never public no
 * matter what the window says, and a PUBLISHED record is public only inside its window.
 */
public enum PublishStatus {
    /** Work in progress. Never public. */
    DRAFT,
    /** Eligible to be public, subject to the publish window. */
    PUBLISHED,
    /** Retired. Never public, but kept for reference and for its URL. */
    ARCHIVED
}
