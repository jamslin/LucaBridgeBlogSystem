package com.lucabridge.core.user;

/**
 * Fixed role set for the CMS (Phase A RBAC).
 * ADMIN  — full access, including user management.
 * EDITOR — content authoring (posts, media, settings), no user management.
 * Add a new constant here to introduce a role; keep SecurityConfig in sync.
 */
public enum Role {
    ADMIN,
    EDITOR
}
