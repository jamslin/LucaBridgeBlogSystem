package com.lucabridge.blog.dto;

public record PostTranslationInput(
        String lang,
        String title,
        String subtitle,
        String excerpt,
        String bodyMarkdown
) {}
