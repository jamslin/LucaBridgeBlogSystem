package com.lucabridge.core.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

    boolean existsByReferenceCode(String referenceCode);

    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);

    List<EventRegistration> findByEventIdOrderBySubmittedAtAsc(Long eventId);

    /**
     * One aggregate query for a whole page of events, not N+1 per row — see EventService for how
     * this feeds the list endpoint. CONFIRMED only, matching RegistrationState's contract.
     */
    @Query("SELECT r.eventId AS eventId, COUNT(r) AS confirmedCount FROM EventRegistration r "
            + "WHERE r.eventId IN :eventIds AND r.status = com.lucabridge.core.event.RegistrationStatus.CONFIRMED "
            + "GROUP BY r.eventId")
    List<ConfirmedCount> countConfirmedByEventIdIn(@Param("eventIds") List<Long> eventIds);

    interface ConfirmedCount {
        Long getEventId();

        long getConfirmedCount();
    }
}
