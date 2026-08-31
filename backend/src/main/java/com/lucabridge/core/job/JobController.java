package com.lucabridge.core.job;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.job.dto.JobDetailDto;
import com.lucabridge.core.job.dto.JobSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public read API. GET only — every write goes through /api/admin/jobs. */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public Page<JobSummaryDto> list(
            @RequestParam(name = "lang", required = false) String rawLang,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Lang lang = Lang.orDefault(rawLang);
        return jobService.listPublished(pageable).map(job -> JobMapper.toSummary(job, lang));
    }

    @GetMapping("/{slug}")
    public JobDetailDto detail(
            @PathVariable String slug,
            @RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return JobMapper.toDetail(jobService.getPublishedBySlug(slug), lang);
    }
}
