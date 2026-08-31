package com.lucabridge.core.media.dto;

import jakarta.validation.constraints.Size;

public record MediaAltTextRequest(@Size(max = 300) String altText) {
}
