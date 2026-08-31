package com.lucabridge.core.homeblock;

import com.lucabridge.core.blog.Blog;
import com.lucabridge.core.homeblock.dto.AdminHomeBlockDto;
import com.lucabridge.core.homeblock.dto.HomeBlockDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.media.Media;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class HomeBlockMapper {

    private HomeBlockMapper() {
    }

    static HomeBlockDto toDto(HomeBlock block, Lang lang) {
        HomeBlockText t = block.getText();
        Media media = block.getMedia();
        Blog blog = block.getBlog();
        return new HomeBlockDto(
                block.getId(),
                block.getSlot(),
                t == null ? null : Localized.pick(lang, t.getTcTitle(), t.getEnTitle(), t.getScTitle()),
                t == null ? null : Localized.pick(lang, t.getTcSubtitle(), t.getEnSubtitle(), t.getScSubtitle()),
                t == null ? null : Localized.pick(lang, t.getTcButtonLabel(), t.getEnButtonLabel(), t.getScButtonLabel()),
                media == null ? null : media.getUrl(),
                media == null ? null : media.getWidth(),
                media == null ? null : media.getHeight(),
                block.getLinkUrl(),
                blog == null ? null : blog.getSlug());
    }

    /**
     * All six slots always present as keys, even with an empty list, so the frontend never
     * needs defensive {@code blocks.HERO ?? []} handling for a slot nobody has populated yet.
     */
    static Map<HomeBlockSlot, List<HomeBlockDto>> toGroupedDto(List<HomeBlock> blocks, Lang lang) {
        Map<HomeBlockSlot, List<HomeBlockDto>> result = new LinkedHashMap<>();
        for (HomeBlockSlot slot : HomeBlockSlot.values()) {
            result.put(slot, new ArrayList<>());
        }
        for (HomeBlock block : blocks) {
            result.get(block.getSlot()).add(toDto(block, lang));
        }
        return result;
    }

    static AdminHomeBlockDto toAdminDto(HomeBlock block) {
        HomeBlockText t = block.getText();
        Media media = block.getMedia();
        Blog blog = block.getBlog();
        return new AdminHomeBlockDto(
                block.getId(),
                block.getSlot(),
                media == null ? null : media.getId(),
                blog == null ? null : blog.getId(),
                block.getLinkUrl(),
                block.getSortOrder(),
                block.isActive(),
                block.getPublishAt(),
                block.getUnpublishAt(),
                block.getUpdatedAt(),
                t == null ? null : t.getTcTitle(),
                t == null ? null : t.getEnTitle(),
                t == null ? null : t.getScTitle(),
                t == null ? null : t.getTcSubtitle(),
                t == null ? null : t.getEnSubtitle(),
                t == null ? null : t.getScSubtitle(),
                t == null ? null : t.getTcButtonLabel(),
                t == null ? null : t.getEnButtonLabel(),
                t == null ? null : t.getScButtonLabel());
    }
}
