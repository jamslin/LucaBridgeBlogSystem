package com.lucabridge.core.blog;

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

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public Page<BlogSummaryDto> list(
            @RequestParam(name = "lang", required = false) String rawLang,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Lang lang = Lang.orDefault(rawLang);
        return blogService.listPublished(pageable).map(blog -> BlogMapper.toSummary(blog, lang));
    }

    @GetMapping("/{slug}")
    public BlogDetailDto detail(
            @PathVariable String slug,
            @RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return BlogMapper.toDetail(blogService.getPublishedBySlug(slug), lang);
    }
}
