package com.lucabridge.core.job;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.job.dto.JobUpsertRequest;
import com.lucabridge.core.publish.PublishStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // ---- public ----

    @Transactional(readOnly = true)
    public Page<Job> listPublished(Pageable pageable) {
        return jobRepository.findVisible(Instant.now(), pageable);
    }

    @Transactional(readOnly = true)
    public Job getPublishedBySlug(String slug) {
        return jobRepository.findVisibleBySlug(slug, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + slug));
    }

    // ---- admin ----

    @Transactional(readOnly = true)
    public Page<Job> listActive(Pageable pageable) {
        return jobRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public Job getActiveById(Long id) {
        return jobRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    @Transactional
    public Job create(JobUpsertRequest req, Long currentUserId) {
        Job job = Job.builder()
                .slug(req.slug())
                .employmentType(req.employmentType())
                .department(req.department())
                .postedAt(req.postedAt())
                .closesAt(req.closesAt())
                .applyEmail(req.applyEmail())
                .applyUrl(req.applyUrl())
                .status(req.status())
                .publishAt(req.publishAt())
                .unpublishAt(req.unpublishAt())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        applyText(job, req);
        applyFirstPublish(job);
        return save(job);
    }

    @Transactional
    public Job update(Long id, JobUpsertRequest req, Long currentUserId) {
        Job job = jobRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        job.setSlug(req.slug());
        job.setEmploymentType(req.employmentType());
        job.setDepartment(req.department());
        job.setPostedAt(req.postedAt());
        job.setClosesAt(req.closesAt());
        job.setApplyEmail(req.applyEmail());
        job.setApplyUrl(req.applyUrl());
        job.setStatus(req.status());
        job.setPublishAt(req.publishAt());
        job.setUnpublishAt(req.unpublishAt());
        job.setUpdatedBy(currentUserId);
        applyText(job, req);
        applyFirstPublish(job);
        return save(job);
    }

    /** Soft delete only: sets deleted_at. Never hard-deleted, for the same reason as blog/event. */
    @Transactional
    public void softDelete(Long id, Long currentUserId) {
        Job job = jobRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        job.setDeletedAt(Instant.now());
        job.setUpdatedBy(currentUserId);
    }

    private void applyText(Job job, JobUpsertRequest req) {
        JobText text = job.getText();
        if (text == null) {
            text = new JobText();
            job.setText(text);
            text.setJob(job);
        }
        text.setTcTitle(req.tcTitle());
        text.setEnTitle(req.enTitle());
        text.setScTitle(req.scTitle());
        text.setTcBody(req.tcBody());
        text.setEnBody(req.enBody());
        text.setScBody(req.scBody());
        text.setTcLocation(req.tcLocation());
        text.setEnLocation(req.enLocation());
        text.setScLocation(req.scLocation());
    }

    /** published_at is set once, on first publish, and never moved by later edits. */
    private void applyFirstPublish(Job job) {
        if (job.getStatus() == PublishStatus.PUBLISHED && job.getPublishedAt() == null) {
            job.setPublishedAt(Instant.now());
        }
    }

    /** Forces the flush so a slug conflict surfaces here as a clean ConflictException — same deferred-write trap as BlogService.save. */
    private Job save(Job job) {
        try {
            Job saved = jobRepository.save(job);
            jobRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Slug already in use: " + job.getSlug());
        }
    }
}
