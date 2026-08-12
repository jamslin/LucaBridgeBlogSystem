package com.lucabridge.blog.dto;

public record JobTranslationInput(
        String lang,
        String title,
        String employmentTypeLabel,
        String summary,
        String bodyMarkdown
) {}
