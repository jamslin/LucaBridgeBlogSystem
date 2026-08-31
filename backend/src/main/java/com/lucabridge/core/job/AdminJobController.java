package com.lucabridge.core.job;

import com.lucabridge.core.job.dto.AdminJobDetailDto;
import com.lucabridge.core.job.dto.AdminJobSummaryDto;
import com.lucabridge.core.job.dto.JobUpsertRequest;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Every route here is under /api/admin/jobs/**, which SecurityConfig restricts to ADMIN/EDITOR
 * (DELETE further narrowed to ADMIN-only) — no per-method role check needed.
 */
@RestController
@RequestMapping("/api/admin/jobs")
public class AdminJobController {

    private final JobService jobService;
    private final CurrentUser currentUser;

    public AdminJobController(JobService jobService, CurrentUser currentUser) {
        this.jobService = jobService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public Page<AdminJobSummaryDto> list(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Instant now = Instant.now();
        return jobService.listActive(pageable).map(job -> JobMapper.toAdminSummary(job, now));
    }

    @GetMapping("/{id}")
    public AdminJobDetailDto get(@PathVariable Long id) {
        return JobMapper.toAdminDetail(jobService.getActiveById(id), Instant.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminJobDetailDto create(@Valid @RequestBody JobUpsertRequest request) {
        Job job = jobService.create(request, currentUser.id());
        return JobMapper.toAdminDetail(job, Instant.now());
    }

    @PutMapping("/{id}")
    public AdminJobDetailDto update(@PathVariable Long id, @Valid @RequestBody JobUpsertRequest request) {
        Job job = jobService.update(id, request, currentUser.id());
        return JobMapper.toAdminDetail(job, Instant.now());
    }

    /** Soft delete only — see JobService.softDelete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobService.softDelete(id, currentUser.id());
    }
}
