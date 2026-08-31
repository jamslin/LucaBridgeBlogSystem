package com.lucabridge.core.event;

import com.lucabridge.core.event.dto.AdminEventDetailDto;
import com.lucabridge.core.event.dto.AdminEventSummaryDto;
import com.lucabridge.core.event.dto.EventUpsertRequest;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Every route here is under /api/admin/events/**, which SecurityConfig restricts to
 * ADMIN/EDITOR (DELETE further narrowed to ADMIN-only) — no per-method role check needed.
 * Reading registrations is a separate, ADMIN-only surface: see AdminEventRegistrationController.
 */
@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final CurrentUser currentUser;

    public AdminEventController(EventService eventService, CurrentUser currentUser) {
        this.eventService = eventService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public Page<AdminEventSummaryDto> list(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Instant now = Instant.now();
        Page<Event> events = eventService.listActive(pageable);
        List<Long> eventIds = events.getContent().stream().map(Event::getId).toList();
        Map<Long, Long> counts = eventService.registeredCounts(eventIds);
        return events.map(event -> EventMapper.toAdminSummary(event, counts.getOrDefault(event.getId(), 0L), now));
    }

    @GetMapping("/{id}")
    public AdminEventDetailDto get(@PathVariable Long id) {
        Event event = eventService.getActiveById(id);
        return EventMapper.toAdminDetail(event, eventService.registeredCount(id), Instant.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminEventDetailDto create(@Valid @RequestBody EventUpsertRequest request) {
        Event event = eventService.create(request, currentUser.id());
        return EventMapper.toAdminDetail(event, 0L, Instant.now());
    }

    @PutMapping("/{id}")
    public AdminEventDetailDto update(@PathVariable Long id, @Valid @RequestBody EventUpsertRequest request) {
        Event event = eventService.update(id, request, currentUser.id());
        return EventMapper.toAdminDetail(event, eventService.registeredCount(id), Instant.now());
    }

    /** Soft delete only — see EventService.softDelete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.softDelete(id, currentUser.id());
    }
}
