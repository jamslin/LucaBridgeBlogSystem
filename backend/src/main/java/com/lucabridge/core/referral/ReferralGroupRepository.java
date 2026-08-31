package com.lucabridge.core.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReferralGroupRepository extends JpaRepository<ReferralGroup, Long> {

    @Query("SELECT g FROM ReferralGroup g LEFT JOIN FETCH g.text WHERE g.active = true ORDER BY g.sortOrder ASC")
    List<ReferralGroup> findAllActive();

    @Query("SELECT g FROM ReferralGroup g LEFT JOIN FETCH g.text ORDER BY g.sortOrder ASC")
    List<ReferralGroup> findAllForAdmin();

    @Query("SELECT g FROM ReferralGroup g LEFT JOIN FETCH g.text WHERE g.id = :id")
    Optional<ReferralGroup> findByIdWithText(@Param("id") Long id);
}
