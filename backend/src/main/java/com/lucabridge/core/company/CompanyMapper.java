package com.lucabridge.core.company;

import com.lucabridge.core.company.dto.AdminCompanyDto;
import com.lucabridge.core.company.dto.CompanyDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.media.Media;

final class CompanyMapper {

    private CompanyMapper() {
    }

    static CompanyDto toDto(Company company, Lang lang) {
        CompanyText t = company.getText();
        Media logo = company.getLogoMedia();
        return new CompanyDto(
                t == null ? null : Localized.pick(lang, t.getTcName(), t.getEnName(), t.getScName()),
                t == null ? null : Localized.pick(lang, t.getTcTagline(), t.getEnTagline(), t.getScTagline()),
                t == null ? null : Localized.pick(lang, t.getTcAbout(), t.getEnAbout(), t.getScAbout()),
                t == null ? null : Localized.pick(lang, t.getTcAddress(), t.getEnAddress(), t.getScAddress()),
                t == null ? null : Localized.pick(lang, t.getTcOfficeHours(), t.getEnOfficeHours(), t.getScOfficeHours()),
                company.getCharityRegNo(),
                company.getFoundedYear(),
                company.getPhone(),
                company.getEmail(),
                logo == null ? null : logo.getUrl(),
                company.getInstagramUrl(),
                company.getFacebookUrl(),
                company.getYoutubeUrl());
    }

    static AdminCompanyDto toAdminDto(Company company) {
        CompanyText t = company.getText();
        Media logo = company.getLogoMedia();
        return new AdminCompanyDto(
                company.getCharityRegNo(),
                company.getFoundedYear(),
                company.getPhone(),
                company.getEmail(),
                logo == null ? null : logo.getId(),
                company.getInstagramUrl(),
                company.getFacebookUrl(),
                company.getYoutubeUrl(),
                t == null ? null : t.getTcName(),
                t == null ? null : t.getEnName(),
                t == null ? null : t.getScName(),
                t == null ? null : t.getTcTagline(),
                t == null ? null : t.getEnTagline(),
                t == null ? null : t.getScTagline(),
                t == null ? null : t.getTcAbout(),
                t == null ? null : t.getEnAbout(),
                t == null ? null : t.getScAbout(),
                t == null ? null : t.getTcAddress(),
                t == null ? null : t.getEnAddress(),
                t == null ? null : t.getScAddress(),
                t == null ? null : t.getTcOfficeHours(),
                t == null ? null : t.getEnOfficeHours(),
                t == null ? null : t.getScOfficeHours(),
                company.getUpdatedAt());
    }
}
