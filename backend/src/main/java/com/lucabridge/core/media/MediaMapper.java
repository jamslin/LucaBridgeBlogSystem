package com.lucabridge.core.media;

import com.lucabridge.core.media.dto.AdminMediaDto;

final class MediaMapper {

    private MediaMapper() {
    }

    static AdminMediaDto toAdminDto(Media media, long usageCount) {
        MediaText text = media.getText();
        return new AdminMediaDto(
                media.getId(),
                media.getUrl(),
                media.getFileName(),
                media.getMimeType(),
                media.getWidth(),
                media.getHeight(),
                media.getByteSize(),
                text == null ? null : text.getTcAlt(),
                usageCount > 0,
                usageCount,
                media.getCreatedAt());
    }
}
