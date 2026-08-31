package com.lucabridge.core.service;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.service.dto.AdminServiceDto;
import com.lucabridge.core.service.dto.ServiceDto;

final class ServiceMapper {

    private ServiceMapper() {
    }

    static ServiceDto toDto(Service service, Lang lang) {
        ServiceText t = service.getText();
        Media icon = service.getIconMedia();
        return new ServiceDto(
                service.getId(),
                service.getCode(),
                t == null ? null : Localized.pick(lang, t.getTcName(), t.getEnName(), t.getScName()),
                t == null ? null : Localized.pick(lang, t.getTcDescription(), t.getEnDescription(), t.getScDescription()),
                icon == null ? null : icon.getUrl());
    }

    static AdminServiceDto toAdminDto(Service service) {
        ServiceText t = service.getText();
        Media icon = service.getIconMedia();
        return new AdminServiceDto(
                service.getId(),
                service.getCode(),
                service.getSortOrder(),
                service.isActive(),
                icon == null ? null : icon.getId(),
                t == null ? null : t.getTcName(),
                t == null ? null : t.getEnName(),
                t == null ? null : t.getScName(),
                t == null ? null : t.getTcDescription(),
                t == null ? null : t.getEnDescription(),
                t == null ? null : t.getScDescription());
    }
}
