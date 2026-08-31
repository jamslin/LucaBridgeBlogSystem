package com.lucabridge.core.media;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.media.dto.AdminMediaDto;
import com.lucabridge.core.media.dto.MediaAltTextRequest;
import com.lucabridge.core.media.dto.MediaSweepPreviewDto;
import com.lucabridge.core.media.dto.MediaSweepResultDto;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Every route here is under /api/admin/media/**, which SecurityConfig restricts to ADMIN/EDITOR
 * (DELETE and the sweep further narrowed to ADMIN-only by the general DELETE rule and the
 * confirm-required guard below — sweep is the one irreversible action in the CMS).
 */
@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;
    private final CurrentUser currentUser;

    public AdminMediaController(MediaService mediaService, CurrentUser currentUser) {
        this.mediaService = mediaService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AdminMediaDto> list() {
        return mediaService.list().stream()
                .map(m -> MediaMapper.toAdminDto(m, mediaService.usageCount(m.getId())))
                .toList();
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminMediaDto upload(@RequestParam("file") MultipartFile file) {
        Media media = mediaService.upload(file, currentUser.id());
        return MediaMapper.toAdminDto(media, 0);
    }

    @PutMapping("/{id}")
    public AdminMediaDto updateAltText(@PathVariable Long id, @Valid @RequestBody MediaAltTextRequest request) {
        Media media = mediaService.updateAltText(id, request.altText());
        return MediaMapper.toAdminDto(media, mediaService.usageCount(id));
    }

    /** Refuses with 409 when the image is still referenced — see MediaService.delete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mediaService.delete(id);
    }

    /** What "Sweep unused" would remove, without removing anything — must be shown before sweep(). */
    @GetMapping("/sweep-preview")
    public MediaSweepPreviewDto sweepPreview() {
        MediaService.SweepOutcome preview = mediaService.previewSweep();
        List<AdminMediaDto> unused = preview.unusedRows().stream()
                .map(m -> MediaMapper.toAdminDto(m, 0))
                .toList();
        return new MediaSweepPreviewDto(unused, preview.orphanObjectKeys());
    }

    /** The one irreversible action in the CMS — refuses without confirm=true. */
    @PostMapping("/sweep")
    public MediaSweepResultDto sweep(@RequestParam(defaultValue = "false") boolean confirm) {
        if (!confirm) {
            throw new ConflictException("Pass confirm=true to sweep unused media — this permanently deletes files from storage");
        }
        MediaService.SweepOutcome result = mediaService.sweep();
        return new MediaSweepResultDto(result.unusedRows().size(), result.orphanObjectKeys().size());
    }
}
