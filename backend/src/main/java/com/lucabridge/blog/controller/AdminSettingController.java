package com.lucabridge.blog.controller;

import com.lucabridge.blog.service.SiteSettingService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin site-settings editor. Protected by SecurityConfig (/api/admin/** = ADMIN or EDITOR).
 * Body is a flat key/value map; each entry is upserted.
 */
@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingController {

    private final SiteSettingService siteSettingService;

    public AdminSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @PutMapping
    public Map<String, String> update(@RequestBody Map<String, String> updates) {
        return siteSettingService.upsert(updates);
    }
}
