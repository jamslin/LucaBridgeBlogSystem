package com.lucabridge.core.event;

import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.event.dto.AdminEventRegistrationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Mounted at /api/admin/registrations, NOT nested under /api/admin/events — SecurityConfig
 * restricts exactly that prefix to ADMIN-only (the general /api/admin/** rule below it allows
 * EDITOR too), and registrants' phone numbers and home addresses must never reach an EDITOR.
 */
@RestController
@RequestMapping("/api/admin/registrations")
public class AdminEventRegistrationController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;

    public AdminEventRegistrationController(EventService eventService, EventRegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping("/events/{eventId}")
    public Page<AdminEventRegistrationDto> list(
            @PathVariable Long eventId,
            @PageableDefault(size = 50, sort = "submittedAt", direction = Sort.Direction.ASC) Pageable pageable) {
        requireEvent(eventId);
        return registrationService.list(eventId, pageable).map(EventMapper::toAdminRegistration);
    }

    @GetMapping("/events/{eventId}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long eventId) {
        requireEvent(eventId);
        List<EventRegistration> registrations = registrationService.listForExport(eventId);
        byte[] csv = toCsv(registrations).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"registrations-event-" + eventId + ".csv\"")
                .body(csv);
    }

    private void requireEvent(Long eventId) {
        if (!eventService.existsActive(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
    }

    private static String toCsv(List<EventRegistration> registrations) {
        StringBuilder sb = new StringBuilder();
        sb.append("reference_code,full_name,gender,birth_year,email,phone,postal_address,"
                + "referral_group_id,referral_group_other,friends_opt_in,whatsapp_confirmed,"
                + "status,locale,submitted_at\n");
        for (EventRegistration r : registrations) {
            sb.append(csv(r.getReferenceCode())).append(',')
                    .append(csv(r.getFullName())).append(',')
                    .append(csv(r.getGender() == null ? "" : r.getGender().name())).append(',')
                    .append(csv(r.getBirthYear() == null ? "" : r.getBirthYear().toString())).append(',')
                    .append(csv(r.getEmail())).append(',')
                    .append(csv(r.getPhone())).append(',')
                    .append(csv(r.getPostalAddress())).append(',')
                    .append(csv(r.getReferralGroupId() == null ? "" : r.getReferralGroupId().toString())).append(',')
                    .append(csv(r.getReferralGroupOther())).append(',')
                    .append(r.isFriendsOptIn()).append(',')
                    .append(r.isWhatsappConfirmed()).append(',')
                    .append(csv(r.getStatus().name())).append(',')
                    .append(csv(r.getLocale())).append(',')
                    .append(csv(r.getSubmittedAt() == null ? "" : r.getSubmittedAt().toString()))
                    .append('\n');
        }
        return sb.toString();
    }

    private static final String FORMULA_TRIGGER_CHARS = "=+-@\t\r";

    /** Quotes for CSV, and neutralises formula injection (a value like "=CMD(...)" opened by an admin in Excel). */
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String safe = !value.isEmpty() && FORMULA_TRIGGER_CHARS.indexOf(value.charAt(0)) >= 0
                ? "'" + value
                : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
