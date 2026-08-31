package com.lucabridge.core.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Used for the last-admin guard: refuse to delete or disable the final enabled ADMIN. */
    @Query("SELECT COUNT(DISTINCT u) FROM AppUser u JOIN u.roles r WHERE u.active = true AND r = com.lucabridge.core.user.Role.ADMIN")
    long countActiveAdmins();
}
