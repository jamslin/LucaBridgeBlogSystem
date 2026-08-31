package com.lucabridge.core.job;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.job.dto.AdminJobDetailDto;
import com.lucabridge.core.job.dto.AdminJobSummaryDto;
import com.lucabridge.core.job.dto.JobDetailDto;
import com.lucabridge.core.job.dto.JobSummaryDto;
import com.lucabridge.core.publish.Visibility;

import java.time.Instant;

/**
 * Entity -> DTO only. Every translated field resolves through {@link Localized#pick}. Uses
 * {@link Visibility#stateOfJob} for the CMS badge, not {@link Visibility#stateOf} — a role past
 * closesAt has already dropped off the public list even with an open publish window.
 */
final class JobMapper {

    private JobMapper() {
    }

    static JobSummaryDto toSummary(Job job, Lang lang) {
        JobText t = job.getText();
        return new JobSummaryDto(
                job.getId(),
                job.getSlug(),
                title(t, lang),
                job.getEmploymentType(),
                job.getDepartment(),
                location(t, lang),
                job.getClosesAt(),
                job.getPublishedAt());
    }

    static JobDetailDto toDetail(Job job, Lang lang) {
        JobText t = job.getText();
        return new JobDetailDto(
                job.getId(),
                job.getSlug(),
                title(t, lang),
                job.getEmploymentType(),
                job.getDepartment(),
                location(t, lang),
                t == null ? null : Localized.pick(lang, t.getTcBody(), t.getEnBody(), t.getScBody()),
                job.getApplyEmail(),
                job.getApplyUrl(),
                job.getClosesAt(),
                job.getPublishedAt());
    }

    static AdminJobSummaryDto toAdminSummary(Job job, Instant now) {
        JobText t = job.getText();
        return new AdminJobSummaryDto(
                job.getId(),
                job.getSlug(),
                t == null ? null : t.getTcTitle(),
                job.getStatus(),
                stateOf(job, now),
                job.getClosesAt(),
                job.getUpdatedAt());
    }

    static AdminJobDetailDto toAdminDetail(Job job, Instant now) {
        JobText t = job.getText();
        return new AdminJobDetailDto(
                job.getId(),
                job.getSlug(),
                job.getEmploymentType(),
                job.getDepartment(),
                job.getPostedAt(),
                job.getClosesAt(),
                job.getApplyEmail(),
                job.getApplyUrl(),
                job.getStatus(),
                stateOf(job, now),
                job.getPublishAt(),
                job.getUnpublishAt(),
                job.getPublishedAt(),
                t == null ? null : t.getTcTitle(),
                t == null ? null : t.getEnTitle(),
                t == null ? null : t.getScTitle(),
                t == null ? null : t.getTcBody(),
                t == null ? null : t.getEnBody(),
                t == null ? null : t.getScBody(),
                t == null ? null : t.getTcLocation(),
                t == null ? null : t.getEnLocation(),
                t == null ? null : t.getScLocation());
    }

    private static String title(JobText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcTitle(), t.getEnTitle(), t.getScTitle());
    }

    private static String location(JobText t, Lang lang) {
        return t == null ? null : Localized.pick(lang, t.getTcLocation(), t.getEnLocation(), t.getScLocation());
    }

    private static Visibility.State stateOf(Job job, Instant now) {
        return Visibility.stateOfJob(job.getStatus(), job.getPublishAt(), job.getUnpublishAt(),
                job.getClosesAt(), job.getDeletedAt(), now);
    }
}
