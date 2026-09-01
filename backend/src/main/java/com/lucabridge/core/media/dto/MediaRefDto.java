package com.lucabridge.core.media.dto;

/**
 * A catalogued image reduced to what an editor screen needs to show it: the id
 * to save back, and the URL to render a thumbnail.
 *
 * The admin DTOs used to carry bare ids, which meant the gallery editor could
 * only draw "#6 #7 #8" placeholders for images already attached — you could not
 * see what you were about to remove.
 */
public record MediaRefDto(Long id, String url) {
}
