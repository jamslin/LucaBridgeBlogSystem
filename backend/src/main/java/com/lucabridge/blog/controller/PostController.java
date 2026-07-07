package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.PostDetailDto;
import com.lucabridge.blog.dto.PostPageDto;
import com.lucabridge.blog.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/posts")
    public PostPageDto list(@RequestParam(required = false) String lang,
                             @RequestParam(required = false) String category,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return postService.listPublished(lang, category, page, size);
    }

    @GetMapping("/api/posts/{slug}")
    public PostDetailDto detail(@PathVariable String slug, @RequestParam(required = false) String lang) {
        return postService.getPublishedBySlug(slug, lang);
    }
}
