package com.lucabridge.core.service;

import com.lucabridge.core.blog.BlogRepository;
import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.event.EventRepository;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.MediaRepository;
import com.lucabridge.core.service.dto.ServiceUpsertRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The class name {@code Service} is already taken by the entity in this package, so the Spring
 * stereotype annotation below is fully qualified rather than imported.
 */
@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final MediaRepository mediaRepository;
    private final BlogRepository blogRepository;
    private final EventRepository eventRepository;

    public ServiceService(ServiceRepository serviceRepository, MediaRepository mediaRepository,
                           BlogRepository blogRepository, EventRepository eventRepository) {
        this.serviceRepository = serviceRepository;
        this.mediaRepository = mediaRepository;
        this.blogRepository = blogRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<Service> listActive() {
        return serviceRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<Service> listAll() {
        return serviceRepository.findAllForAdmin();
    }

    @Transactional(readOnly = true)
    public Service get(Long id) {
        return serviceRepository.findByIdWithText(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
    }

    @Transactional
    public Service create(ServiceUpsertRequest req) {
        Service service = Service.builder()
                .code(req.code())
                .sortOrder(req.sortOrder() == null ? 0 : req.sortOrder())
                .active(req.active())
                .build();
        applyIcon(service, req.iconMediaId());
        applyText(service, req);
        return save(service);
    }

    @Transactional
    public Service update(Long id, ServiceUpsertRequest req) {
        Service service = get(id);
        service.setCode(req.code());
        service.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        service.setActive(req.active());
        applyIcon(service, req.iconMediaId());
        applyText(service, req);
        return save(service);
    }

    /** What the delete confirmation dialog needs — how many live posts/events would lose this chip. */
    @Transactional(readOnly = true)
    public Usage usage(Long id) {
        long blogCount = blogRepository.countByServiceIdAndDeletedAtIsNull(id);
        long eventCount = eventRepository.countByServiceIdAndDeletedAtIsNull(id);
        return new Usage(blogCount, eventCount);
    }

    /**
     * Doesn't touch blog.service_id or event.service_id — the FK's ON DELETE SET NULL does that
     * automatically. Refuses without confirm=true when either count is nonzero, so a delete
     * can't silently make chips vanish from live content.
     */
    @Transactional
    public void delete(Long id, boolean confirm) {
        Service service = get(id);
        Usage usage = usage(id);
        if ((usage.blogCount() > 0 || usage.eventCount() > 0) && !confirm) {
            throw new ConflictException("Service is referenced by " + usage.blogCount()
                    + " blog post(s) and " + usage.eventCount()
                    + " event(s) — pass confirm=true to delete anyway");
        }
        serviceRepository.delete(service);
    }

    public record Usage(long blogCount, long eventCount) {
    }

    private void applyIcon(Service service, Long iconMediaId) {
        if (iconMediaId == null) {
            service.setIconMedia(null);
            return;
        }
        Media media = mediaRepository.findById(iconMediaId)
                .orElseThrow(() -> new BadRequestException("Unknown media: " + iconMediaId));
        service.setIconMedia(media);
    }

    private void applyText(Service service, ServiceUpsertRequest req) {
        ServiceText text = service.getText();
        if (text == null) {
            text = new ServiceText();
            service.setText(text);
            text.setService(service);
        }
        text.setTcName(req.tcName());
        text.setEnName(req.enName());
        text.setScName(req.scName());
        text.setTcDescription(req.tcDescription());
        text.setEnDescription(req.enDescription());
        text.setScDescription(req.scDescription());
    }

    /** Forces the flush so a code conflict surfaces here as a clean ConflictException — same deferred-write trap as BlogService.save. */
    private Service save(Service service) {
        try {
            Service saved = serviceRepository.save(service);
            serviceRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Service code already in use: " + service.getCode());
        }
    }
}
