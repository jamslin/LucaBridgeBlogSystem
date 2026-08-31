package com.lucabridge.core.event;

import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.event.dto.EventRegistrationRequest;
import com.lucabridge.core.event.dto.EventRegistrationResponse;
import com.lucabridge.core.i18n.Lang;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EventRegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public EventRegistrationService(EventRepository eventRepository,
                                     EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    /**
     * Locks the event row for this whole method (see EventRepository.findByIdForUpdate), so two
     * concurrent submissions for the SAME event serialize: whichever gets the lock second
     * re-counts CONFIRMED registrations only after the first has committed, and so correctly
     * sees itself as the one that goes to the waitlist. Submissions for different events never
     * contend with each other.
     */
    @Transactional
    public EventRegistrationResponse submit(Long eventId, EventRegistrationRequest req) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        long confirmedCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
        RegistrationState state = RegistrationState.of(event.isRegisterable(), event.getRegistrationOpensAt(),
                event.getRegistrationClosesAt(), event.getCapacity(), confirmedCount, Instant.now());

        if (state == RegistrationState.NOT_REGISTERABLE || state == RegistrationState.NOT_OPEN
                || state == RegistrationState.CLOSED) {
            throw new BadRequestException("Registration is not open for this event");
        }

        // Race-safe: still inside the same event-row lock as the count above, so a concurrent
        // submission with the same email for this event cannot both pass this check.
        if (registrationRepository.existsByEventIdAndEmailIgnoreCase(eventId, req.email())) {
            throw new ConflictException("This email has already registered for this event");
        }

        RegistrationStatus status = state == RegistrationState.FULL
                ? RegistrationStatus.WAITLIST
                : RegistrationStatus.CONFIRMED;

        Instant now = Instant.now();
        EventRegistration registration = EventRegistration.builder()
                .eventId(eventId)
                .referenceCode(generateUniqueReferenceCode())
                .referralGroupId(req.referralGroupId())
                .referralGroupOther(req.referralGroupOther())
                .fullName(req.fullName())
                .gender(req.gender())
                .birthYear(req.birthYear())
                .email(req.email())
                .phone(req.phone())
                .postalAddress(req.postalAddress())
                .status(status)
                .locale(Lang.orDefault(req.lang()).code())
                .termsAcceptedAt(now)
                .termsVersion(req.termsVersion())
                .privacyConsentAt(now)
                .privacyVersion(req.privacyVersion())
                .friendsOptIn(req.friendsOptIn())
                .build();

        try {
            registrationRepository.save(registration);
            registrationRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Both the email and reference-code uniqueness were already checked above under the
            // event-row lock; reaching here means a residual race slipped through (or, for
            // reference_code, that the retry loop below still lost the coin flip). Either way
            // it's a conflict, not a 500 — but don't blame it on the email specifically when
            // it might not be.
            throw new ConflictException("Could not complete registration due to a conflict — please try again");
        }

        return new EventRegistrationResponse(registration.getReferenceCode(), status);
    }

    // ---- admin ----

    @Transactional(readOnly = true)
    public Page<EventRegistration> list(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable);
    }

    @Transactional(readOnly = true)
    public List<EventRegistration> listForExport(Long eventId) {
        return registrationRepository.findByEventIdOrderBySubmittedAtAsc(eventId);
    }

    /** Pre-checked against the DB rather than relying solely on the save()/flush() catch, so a collision is deterministic, not a thrown-and-caught exception on the happy path. */
    private String generateUniqueReferenceCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = ReferenceCodeGenerator.generate();
            if (!registrationRepository.existsByReferenceCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique reference code after 5 attempts");
    }
}
