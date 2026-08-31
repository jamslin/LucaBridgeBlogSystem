package com.lucabridge.core.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /** Callers pass RegistrationStatus.OCCUPIES_CAPACITY for a capacity-relevant count — see EventService/EventRegistrationService. */
    long countByEventIdAndStatusIn(Long eventId, Collection<RegistrationStatus> statuses);

    boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

    boolean existsByReferenceCode(String referenceCode);

    long countByReferralGroupId(Long referralGroupId);

    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);

    List<EventRegistration> findByEventIdOrderBySubmittedAtAsc(Long eventId);

    /**
     * One aggregate query for a whole page of events, not N+1 per row — see EventService for how
     * this feeds the list endpoint. CONFIRMED + ATTENDED: both occupy a place against capacity,
     * see RegistrationStatus.OCCUPIES_CAPACITY.
     */
    @Query("SELECT r.eventId AS eventId, COUNT(r) AS occupiedCount FROM EventRegistration r "
            + "WHERE r.eventId IN :eventIds AND r.status IN "
            + "(com.lucabridge.core.event.RegistrationStatus.CONFIRMED, com.lucabridge.core.event.RegistrationStatus.ATTENDED) "
            + "GROUP BY r.eventId")
    List<OccupiedCount> countOccupiedByEventIdIn(@Param("eventIds") List<Long> eventIds);

    interface OccupiedCount {
        Long getEventId();

        long getOccupiedCount();
    }
}
