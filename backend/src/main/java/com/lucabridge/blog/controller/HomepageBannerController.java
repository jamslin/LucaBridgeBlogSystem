package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.HomepageBannerDto;
import com.lucabridge.blog.service.HomepageBannerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class HomepageBannerController {
    private final HomepageBannerService service;
    public HomepageBannerController(HomepageBannerService service) { this.service = service; }
    @GetMapping("/api/banners") public List<HomepageBannerDto> list(@RequestParam(required = false) String lang) { return service.publicList(lang); }
}
