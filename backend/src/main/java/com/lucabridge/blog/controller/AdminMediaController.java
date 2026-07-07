package com.lucabridge.blog.controller;

import com.lucabridge.blog.service.MediaStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Admin media upload — TODO per Coding Plan Phase 4 (MinIO upload service is build-ready,
 * but this endpoint has not yet been wired to the admin SPA's drag-drop uploader).
 */
@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaStorageService mediaStorageService;

    public AdminMediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = mediaStorageService.upload(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
