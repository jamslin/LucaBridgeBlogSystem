package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.HomepageBanner;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.HomepageBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class HomepageBannerService {
    private final HomepageBannerRepository repo;
    private final LocalizationService localization;
    public HomepageBannerService(HomepageBannerRepository repo, LocalizationService localization) { this.repo = repo; this.localization = localization; }

    @Transactional(readOnly = true)
    public List<HomepageBannerDto> publicList(String lang) {
        String normalized = localization.normalize(lang); Instant now = Instant.now();
        return repo.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(b -> b.isActive() && (b.getStartsAt() == null || !b.getStartsAt().isAfter(now)) && (b.getEndsAt() == null || !b.getEndsAt().isBefore(now)))
                .map(b -> publicDto(b, normalized)).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminHomepageBannerDto> adminList() { return repo.findAllByOrderBySortOrderAscIdAsc().stream().map(this::adminDto).toList(); }

    @Transactional
    public Long upsert(HomepageBannerUpsertRequest r) {
        if (r.startsAt() != null && r.endsAt() != null && r.endsAt().isBefore(r.startsAt())) throw new BadRequestException("Banner end must not be before start");
        HomepageBanner b = r.id() == null ? new HomepageBanner() : repo.findById(r.id()).orElseThrow(() -> new ResourceNotFoundException("Banner not found: " + r.id()));
        b.setImageUrl(r.imageUrl().trim()); b.setLinkUrl(blank(r.linkUrl())); b.setSortOrder(r.sortOrder()); b.setActive(r.active()); b.setStartsAt(r.startsAt()); b.setEndsAt(r.endsAt());
        b.setTitleZhHant(r.titleZhHant().trim()); b.setSubtitleZhHant(blank(r.subtitleZhHant())); b.setButtonLabelZhHant(blank(r.buttonLabelZhHant()));
        b.setTitleEn(blank(r.titleEn())); b.setSubtitleEn(blank(r.subtitleEn())); b.setButtonLabelEn(blank(r.buttonLabelEn()));
        b.setTitleZhHans(blank(r.titleZhHans())); b.setSubtitleZhHans(blank(r.subtitleZhHans())); b.setButtonLabelZhHans(blank(r.buttonLabelZhHans()));
        return repo.save(b).getId();
    }

    @Transactional public void delete(Long id) { HomepageBanner b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner not found: " + id)); repo.delete(b); }

    private HomepageBannerDto publicDto(HomepageBanner b, String lang) {
        String title = b.getTitleZhHant(), subtitle = b.getSubtitleZhHant(), button = b.getButtonLabelZhHant();
        if ("en".equals(lang) && b.getTitleEn() != null) { title = b.getTitleEn(); subtitle = b.getSubtitleEn(); button = b.getButtonLabelEn(); }
        else if ("zh-Hans".equals(lang) && b.getTitleZhHans() != null) { title = b.getTitleZhHans(); subtitle = b.getSubtitleZhHans(); button = b.getButtonLabelZhHans(); }
        return new HomepageBannerDto(b.getId(), b.getImageUrl(), b.getLinkUrl(), b.getSortOrder(), title, subtitle, button);
    }

    private AdminHomepageBannerDto adminDto(HomepageBanner b) { return new AdminHomepageBannerDto(b.getId(), b.getImageUrl(), b.getLinkUrl(), b.getSortOrder(), b.isActive(), b.getStartsAt(), b.getEndsAt(), b.getTitleZhHant(), b.getSubtitleZhHant(), b.getButtonLabelZhHant(), b.getTitleEn(), b.getSubtitleEn(), b.getButtonLabelEn(), b.getTitleZhHans(), b.getSubtitleZhHans(), b.getButtonLabelZhHans()); }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
