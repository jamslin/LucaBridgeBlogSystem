package com.lucabridge.core.blog.dto;

import java.time.Instant;

/**
 * The 上一篇 / 下一篇 cards at the foot of an article (mockup 8d). Only what those cards
 * render — no cover, no body — so walking the archive costs one small row each way.
 */
public record BlogNeighbourDto(String slug, String title, String serviceName, Instant publishedAt) {
}
