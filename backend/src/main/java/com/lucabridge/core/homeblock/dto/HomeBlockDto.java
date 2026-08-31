package com.lucabridge.core.homeblock.dto;

import com.lucabridge.core.homeblock.HomeBlockSlot;

/** Public view — one language, already resolved via Localized.pick(). blogSlug is null unless this block pins a story (FEATURED). */
public record HomeBlockDto(
        Long id,
        HomeBlockSlot slot,
        String title,
        String subtitle,
        String buttonLabel,
        String mediaUrl,
        Integer mediaWidth,
        Integer mediaHeight,
        String linkUrl,
        String blogSlug) {
}
