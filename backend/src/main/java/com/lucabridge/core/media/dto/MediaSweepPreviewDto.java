package com.lucabridge.core.media.dto;

import java.util.List;

/** What the sweep confirmation dialog shows before MediaService.sweep() actually deletes anything. */
public record MediaSweepPreviewDto(List<AdminMediaDto> unusedMedia, List<String> orphanObjectKeys) {
}
