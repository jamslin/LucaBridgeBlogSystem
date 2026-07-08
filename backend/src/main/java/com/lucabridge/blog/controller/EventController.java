package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.EventDetailDto;
import com.lucabridge.blog.dto.EventSummaryDto;
import com.lucabridge.blog.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/api/events")
    public List<EventSummaryDto> list(@RequestParam(required = false) String lang) {
        return eventService.listEvents(lang);
    }

    @GetMapping("/api/events/{slug}")
    public EventDetailDto detail(@PathVariable String slug, @RequestParam(required = false) String lang) {
        return eventService.getBySlug(slug, lang);
    }
}
