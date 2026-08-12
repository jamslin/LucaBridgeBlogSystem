package com.lucabridge.blog.controller;

import com.lucabridge.blog.dto.MediaAssetDto;
import com.lucabridge.blog.dto.UpdateMediaRequest;
import com.lucabridge.blog.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Media Library — upload (catalogues the file), list with usage detection,
 * edit alt text, and delete (blocked if the image is still referenced by a post).
 * Protected by SecurityConfig (/api/admin/** = ADMIN or EDITOR).
 */
@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;

    public AdminMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    public MediaAssetDto upload(@RequestParam("file") MultipartFile file) {
        return mediaService.upload(file);
    }

    @GetMapping
    public List<MediaAssetDto> list() {
        return mediaService.list();
    }

    @PostMapping("/sync")
    public java.util.Map<String, Integer> sync() {
        return java.util.Map.of("added", mediaService.syncFromStorage());
    }

    @PutMapping("/{id}")
    public MediaAssetDto update(@PathVariable Long id, @Valid @RequestBody UpdateMediaRequest request) {
        return mediaService.updateAlt(id, request.altText());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
