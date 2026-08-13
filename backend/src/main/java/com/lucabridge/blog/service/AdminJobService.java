package com.lucabridge.blog.service;

import com.lucabridge.blog.dto.*;
import com.lucabridge.blog.entity.JobPosting;
import com.lucabridge.blog.entity.JobPostingTranslation;
import com.lucabridge.blog.exception.BadRequestException;
import com.lucabridge.blog.exception.ConflictException;
import com.lucabridge.blog.exception.ResourceNotFoundException;
import com.lucabridge.blog.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Admin CRUD for Job Postings. */
@Service
public class AdminJobService {

    private final JobPostingRepository repo;
    private final LocalizationService localization;

    public AdminJobService(JobPostingRepository repo, LocalizationService localization) {
        this.repo = repo;
        this.localization = localization;
    }

    @Transactional(readOnly = true)
    public List<AdminJobSummaryDto> list() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(JobPosting::getId).reversed())
                .map(j -> new AdminJobSummaryDto(j.getId(), j.getSlug(), j.getStatus(), titleOf(j), j.getDepartment(), j.getPostedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminJobDetailDto getForEdit(Long id) {
        JobPosting j = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        List<JobTranslationInput> tr = j.getTranslations().stream()
                .map(t -> new JobTranslationInput(t.getLang(), t.getTitle(), t.getEmploymentTypeLabel(), t.getSummary(), t.getBodyMarkdown()))
                .toList();
        return new AdminJobDetailDto(j.getId(), j.getSlug(), j.getStatus(), j.getEmploymentType(), j.getDepartment(),
                j.getLocationText(), j.getPostedAt(), j.getClosesAt(), tr);
    }

    @Transactional
    public Long upsert(JobUpsertRequest req) {
        JobPosting j = req.id() != null
                ? repo.findById(req.id()).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + req.id()))
                : JobPosting.builder().status("DRAFT").build();
        String slug = req.slug().trim();
        boolean duplicate = j.getId() == null ? repo.existsBySlug(slug) : repo.existsBySlugAndIdNot(slug, j.getId());
        if (duplicate) throw new ConflictException("Job slug already exists: " + slug);
        if (req.postedAt() != null && req.closesAt() != null && req.closesAt().isBefore(req.postedAt())) {
            throw new BadRequestException("Closing date must not be before posting date");
        }
        j.setSlug(slug);
        j.setStatus("DRAFT");
        j.setEmploymentType(req.employmentType());
        j.setDepartment(req.department());
        j.setLocationText(req.locationText());
        j.setPostedAt(req.postedAt());
        j.setClosesAt(req.closesAt());

        Map<String, JobPostingTranslation> existing = new HashMap<>();
        for (JobPostingTranslation t : j.getTranslations()) existing.put(t.getLang(), t);
        Set<String> langs = new HashSet<>();
        for (JobTranslationInput in : req.translations()) {
            if (in.title() == null || in.title().isBlank()) continue;
            langs.add(in.lang());
            JobPostingTranslation t = existing.get(in.lang());
            if (t == null) { t = JobPostingTranslation.builder().jobPosting(j).lang(in.lang()).build(); j.getTranslations().add(t); }
            t.setTitle(in.title());
            t.setEmploymentTypeLabel(in.employmentTypeLabel());
            t.setSummary(in.summary());
            t.setBodyMarkdown(in.bodyMarkdown() != null ? in.bodyMarkdown() : "");
        }
        if (langs.isEmpty()) throw new BadRequestException("At least one translation with a title is required");
        j.getTranslations().removeIf(t -> !langs.contains(t.getLang()));
        return repo.save(j).getId();
    }

    @Transactional
    public void publish(Long id) {
        JobPosting j = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        if (j.getPostedAt() != null && j.getClosesAt() != null && j.getClosesAt().isBefore(j.getPostedAt())) {
            throw new BadRequestException("Closing date must not be before posting date");
        }
        validateTranslationsForPublish(j.getTranslations());
        j.setStatus("PUBLISHED");
        repo.save(j);
    }

    @Transactional
    public void unpublish(Long id) {
        JobPosting j = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        j.setStatus("DRAFT");
        repo.save(j);
    }

    private void validateTranslationsForPublish(List<JobPostingTranslation> translations) {
        JobPostingTranslation required = translations.stream().filter(t -> localization.defaultLang().equals(t.getLang()))
                .findFirst().orElseThrow(() -> new BadRequestException("Cannot publish without a " + localization.defaultLang() + " translation"));
        if (!complete(required)) throw new BadRequestException("The " + localization.defaultLang() + " translation needs a title and body");
        if (translations.stream().anyMatch(t -> !complete(t))) throw new BadRequestException("Every saved translation needs a title and body before publishing");
    }

    private boolean complete(JobPostingTranslation t) {
        return t.getTitle() != null && !t.getTitle().isBlank() && t.getBodyMarkdown() != null && !t.getBodyMarkdown().isBlank();
    }

    @Transactional
    public void delete(Long id) {
        JobPosting j = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        repo.delete(j);
    }

    private String titleOf(JobPosting j) {
        String def = localization.defaultLang();
        String fallback = null;
        for (JobPostingTranslation t : j.getTranslations()) {
            if (fallback == null) fallback = t.getTitle();
            if (def.equals(t.getLang())) return t.getTitle();
        }
        return fallback != null ? fallback : j.getSlug();
    }
}
