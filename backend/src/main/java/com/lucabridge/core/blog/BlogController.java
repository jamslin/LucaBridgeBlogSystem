package com.lucabridge.core.blog;

import java.util.Map;

import com.lucabridge.core.service.ServiceLabels;

import com.lucabridge.core.blog.dto.BlogDetailDto;
import com.lucabridge.core.blog.dto.BlogSummaryDto;
import com.lucabridge.core.i18n.Lang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public read API. GET only — every write goes through /api/admin/blog. */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;
    private final ServiceLabels serviceLabels;

    public BlogController(BlogService blogService, ServiceLabels serviceLabels) {
        this.blogService = blogService;
        this.serviceLabels = serviceLabels;
    }

    @GetMapping
    public Page<BlogSummaryDto> list(
            @RequestParam(name = "lang", required = false) String rawLang,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Lang lang = Lang.orDefault(rawLang);
        // Resolved once for the page rather than per row.
        Map<Long, String> names = serviceLabels.namesBy(lang);
        return blogService.listPublished(pageable)
                .map(blog -> BlogMapper.toSummary(blog, lang, names.get(blog.getServiceId())));
    }

    @GetMapping("/{slug}")
    public BlogDetailDto detail(
            @PathVariable String slug,
            @RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        Blog blog = blogService.getPublishedBySlug(slug);
        Map<Long, String> names = serviceLabels.namesBy(lang);
        Blog older = blogService.findOlder(blog.getPublishedAt());
        Blog newer = blogService.findNewer(blog.getPublishedAt());
        return BlogMapper.toDetail(blog, lang, names.get(blog.getServiceId()),
                BlogMapper.toNeighbour(older, lang, older == null ? null : names.get(older.getServiceId())),
                BlogMapper.toNeighbour(newer, lang, newer == null ? null : names.get(newer.getServiceId())));
    }
}
