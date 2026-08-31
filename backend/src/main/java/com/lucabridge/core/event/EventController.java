package com.lucabridge.core.event;

import com.lucabridge.core.error.TooManyRequestsException;
import com.lucabridge.core.event.dto.EventDetailDto;
import com.lucabridge.core.event.dto.EventRegistrationRequest;
import com.lucabridge.core.event.dto.EventRegistrationResponse;
import com.lucabridge.core.event.dto.EventSummaryDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.security.ClientIp;
import com.lucabridge.core.security.RegistrationRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Public read API, plus the one public write on the whole site (registration submission). */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;
    private final RegistrationRateLimiter rateLimiter;

    public EventController(EventService eventService, EventRegistrationService registrationService,
                            RegistrationRateLimiter rateLimiter) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Ignores any client-supplied sort: findUpcomingVisible's ORDER BY (startsAt ASC NULLS
     * LAST) can't be expressed as a Sort, so an unsorted Pageable is built here on purpose
     * rather than trusting @PageableDefault/@SortDefault not to collide with it.
     */
    @GetMapping
    public Page<EventSummaryDto> list(
            @RequestParam(name = "lang", required = false) String rawLang,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Lang lang = Lang.orDefault(rawLang);
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.listUpcoming(pageable);
        Instant now = Instant.now();
        List<Long> eventIds = events.getContent().stream().map(Event::getId).toList();
        Map<Long, Long> counts = eventService.registeredCounts(eventIds);
        return events.map(event -> EventMapper.toSummary(event, lang, counts.getOrDefault(event.getId(), 0L), now));
    }

    @GetMapping("/{slug}")
    public EventDetailDto detail(
            @PathVariable String slug,
            @RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        Event event = eventService.getPublishedBySlug(slug);
        long confirmedCount = eventService.registeredCount(event.getId());
        return EventMapper.toDetail(event, lang, confirmedCount, Instant.now());
    }

    /**
     * Public, rate-limited, spam-guarded — the site's first unauthenticated write. Capacity vs
     * waitlist is decided inside EventRegistrationService.submit under a row lock; this method
     * only gates on request volume per IP before that.
     */
    @PostMapping("/{id}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRegistrationResponse register(
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationRequest request,
            HttpServletRequest httpRequest) {
        if (!rateLimiter.tryAcquire(ClientIp.resolve(httpRequest))) {
            throw new TooManyRequestsException("Too many registration attempts — please try again later");
        }
        return registrationService.submit(id, request);
    }
}
