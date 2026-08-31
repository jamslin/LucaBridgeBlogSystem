package com.lucabridge.core.service.dto;

/** Public view — one language, already resolved via Localized.pick(). Drives the home chip row, the home service cards, blog/event tagging, and the services page. */
public record ServiceDto(Long id, String code, String name, String description, String iconUrl) {
}
