package com.lucabridge.blog.dto;

public record EventTranslationInput(
        String lang,
        String title,
        String summary,
        String bodyMarkdown
) {}
