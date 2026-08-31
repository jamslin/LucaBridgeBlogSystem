package com.lucabridge.core.referral;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.referral.dto.AdminReferralGroupDto;
import com.lucabridge.core.referral.dto.ReferralGroupDto;

final class ReferralGroupMapper {

    private ReferralGroupMapper() {
    }

    static ReferralGroupDto toDto(ReferralGroup group, Lang lang) {
        ReferralGroupText t = group.getText();
        return new ReferralGroupDto(
                group.getId(),
                group.getCode(),
                t == null ? null : Localized.pick(lang, t.getTcName(), t.getEnName(), t.getScName()));
    }

    static AdminReferralGroupDto toAdminDto(ReferralGroup group) {
        ReferralGroupText t = group.getText();
        return new AdminReferralGroupDto(
                group.getId(),
                group.getCode(),
                group.getSortOrder(),
                group.isActive(),
                t == null ? null : t.getTcName(),
                t == null ? null : t.getEnName(),
                t == null ? null : t.getScName());
    }
}
