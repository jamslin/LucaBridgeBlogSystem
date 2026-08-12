package com.lucabridge.blog.dto;

import jakarta.validation.constraints.Size;

public record UpdateMediaRequest(
        @Size(max = 500) String altText
) {}
