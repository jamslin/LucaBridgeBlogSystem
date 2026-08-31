package com.lucabridge.core.homeblock;

import com.lucabridge.core.blog.Blog;
import com.lucabridge.core.blog.BlogRepository;
import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.homeblock.dto.HomeBlockUpsertRequest;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HomeBlockService {

    private final HomeBlockRepository homeBlockRepository;
    private final MediaRepository mediaRepository;
    private final BlogRepository blogRepository;

    public HomeBlockService(HomeBlockRepository homeBlockRepository, MediaRepository mediaRepository,
                             BlogRepository blogRepository) {
        this.homeBlockRepository = homeBlockRepository;
        this.mediaRepository = mediaRepository;
        this.blogRepository = blogRepository;
    }

    @Transactional(readOnly = true)
    public List<HomeBlock> listVisible() {
        return homeBlockRepository.findAllVisible(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<HomeBlock> listAll() {
        return homeBlockRepository.findAllForAdmin();
    }

    @Transactional(readOnly = true)
    public HomeBlock get(Long id) {
        return homeBlockRepository.findByIdWithText(id)
                .orElseThrow(() -> new ResourceNotFoundException("Home block not found: " + id));
    }

    @Transactional
    public HomeBlock create(HomeBlockUpsertRequest req, Long currentUserId) {
        HomeBlock block = HomeBlock.builder()
                .slot(req.slot())
                .linkUrl(req.linkUrl())
                .sortOrder(req.sortOrder() == null ? 0 : req.sortOrder())
                .active(req.active())
                .publishAt(req.publishAt())
                .unpublishAt(req.unpublishAt())
                .updatedBy(currentUserId)
                .build();
        applyMedia(block, req.mediaId());
        applyBlog(block, req.blogId());
        applyText(block, req);
        return homeBlockRepository.save(block);
    }

    @Transactional
    public HomeBlock update(Long id, HomeBlockUpsertRequest req, Long currentUserId) {
        HomeBlock block = get(id);
        block.setSlot(req.slot());
        block.setLinkUrl(req.linkUrl());
        block.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        block.setActive(req.active());
        block.setPublishAt(req.publishAt());
        block.setUnpublishAt(req.unpublishAt());
        block.setUpdatedBy(currentUserId);
        applyMedia(block, req.mediaId());
        applyBlog(block, req.blogId());
        applyText(block, req);
        return homeBlockRepository.save(block);
    }

    /** Hard delete — home_block has no unique business key like slug/code for another row to collide with, and nothing else references a home_block row. */
    @Transactional
    public void delete(Long id) {
        homeBlockRepository.delete(get(id));
    }

    private void applyMedia(HomeBlock block, Long mediaId) {
        if (mediaId == null) {
            block.setMedia(null);
            return;
        }
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new BadRequestException("Unknown media: " + mediaId));
        block.setMedia(media);
    }

    private void applyBlog(HomeBlock block, Long blogId) {
        if (blogId == null) {
            block.setBlog(null);
            return;
        }
        Blog blog = blogRepository.findActiveById(blogId)
                .orElseThrow(() -> new BadRequestException("Unknown blog: " + blogId));
        block.setBlog(blog);
    }

    private void applyText(HomeBlock block, HomeBlockUpsertRequest req) {
        HomeBlockText text = block.getText();
        if (text == null) {
            text = new HomeBlockText();
            block.setText(text);
            text.setHomeBlock(block);
        }
        text.setTcTitle(req.tcTitle());
        text.setEnTitle(req.enTitle());
        text.setScTitle(req.scTitle());
        text.setTcSubtitle(req.tcSubtitle());
        text.setEnSubtitle(req.enSubtitle());
        text.setScSubtitle(req.scSubtitle());
        text.setTcButtonLabel(req.tcButtonLabel());
        text.setEnButtonLabel(req.enButtonLabel());
        text.setScButtonLabel(req.scButtonLabel());
    }
}
