package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.JobDetailDto;
import com.lucabridge.blog.dto.JobSummaryDto;
import com.lucabridge.blog.service.JobPostingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping("/api/jobs")
    public List<JobSummaryDto> list(@RequestParam(required = false) String lang) {
        return jobPostingService.listJobs(lang);
    }

    @GetMapping("/api/jobs/{slug}")
    public JobDetailDto detail(@PathVariable String slug, @RequestParam(required = false) String lang) {
        return jobPostingService.getBySlug(slug, lang);
    }
}
