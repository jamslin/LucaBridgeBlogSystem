package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.JobDetailDto;
import com.lucabridge.blog.dto.JobSummaryDto;
import com.lucabridge.blog.entity.JobPosting;
import com.lucabridge.blog.entity.JobPostingTranslation;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobPostingService {

    private static final String PUBLISHED = "PUBLISHED";

    private final JobPostingRepository jobPostingRepository;
    private final LocalizationService localizationService;

    public JobPostingService(JobPostingRepository jobPostingRepository, LocalizationService localizationService) {
        this.jobPostingRepository = jobPostingRepository;
        this.localizationService = localizationService;
    }

    @Transactional(readOnly = true)
    public List<JobSummaryDto> listJobs(String lang) {
        String normalizedLang = localizationService.normalize(lang);
        return jobPostingRepository.findByStatusOrderByPostedAtDesc(PUBLISHED).stream()
                .map(j -> {
                    var resolved = localizationService.resolve(
                            j.getTranslations(), normalizedLang, JobPostingTranslation::getLang);
                    String title = resolved.map(r -> r.value().getTitle()).orElse(j.getSlug());
                    String summary = resolved.map(r -> r.value().getSummary()).orElse(null);
                    String typeLabel = resolved.map(r -> r.value().getEmploymentTypeLabel()).orElse(null);
                    boolean fallback = resolved.map(LocalizationService.Resolved::fallback).orElse(false);
                    return new JobSummaryDto(j.getId(), j.getSlug(), title, summary,
                            j.getDepartment(), j.getEmploymentType(), typeLabel,
                            j.getLocationText(), j.getPostedAt(), j.getClosesAt(), fallback);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDetailDto getBySlug(String slug, String lang) {
        String normalizedLang = localizationService.normalize(lang);
        JobPosting job = jobPostingRepository.findBySlugAndStatus(slug, PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + slug));

        var resolved = localizationService.resolve(job.getTranslations(), normalizedLang, JobPostingTranslation::getLang)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting has no usable translation: " + slug));

        JobPostingTranslation t = resolved.value();
        return new JobDetailDto(job.getId(), job.getSlug(), t.getTitle(), t.getSummary(), t.getBodyMarkdown(),
                job.getDepartment(), job.getEmploymentType(), t.getEmploymentTypeLabel(),
                job.getLocationText(), job.getPostedAt(), job.getClosesAt(), resolved.fallback());
    }
}
