package com.lucabridge.blog.dto;

public record PageTranslationInput(
        String lang,
        String title,
        String subtitle,
        String bodyMarkdown
) {}
