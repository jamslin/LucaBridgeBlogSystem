package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.PageDto;
import com.lucabridge.blog.dto.PageSummaryDto;
import com.lucabridge.blog.service.PageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/api/pages")
    public List<PageSummaryDto> list(@RequestParam(required = false) String lang) {
        return pageService.listPages(lang);
    }

    @GetMapping("/api/pages/{slug}")
    public PageDto detail(@PathVariable String slug, @RequestParam(required = false) String lang) {
        return pageService.getBySlug(slug, lang);
    }
}
