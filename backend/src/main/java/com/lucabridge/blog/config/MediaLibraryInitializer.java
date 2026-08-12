package com.lucabridge.blog.config;

import com.lucabridge.blog.service.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * On startup, catalogue any images already in the bucket that have no MediaAsset row yet
 * (e.g. seeded/imported images). Runs after schema + seed. Failures (e.g. storage briefly
 * unreachable) are logged and never abort boot.
 */
@Component
@Order(100)
public class MediaLibraryInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MediaLibraryInitializer.class);

    private final MediaService mediaService;

    public MediaLibraryInitializer(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public void run(String... args) {
        try {
            int added = mediaService.syncFromStorage();
            if (added > 0) log.info("Media library: catalogued {} existing bucket object(s)", added);
        } catch (Exception e) {
            log.warn("Media library backfill skipped: {}", e.getMessage());
        }
    }
}
