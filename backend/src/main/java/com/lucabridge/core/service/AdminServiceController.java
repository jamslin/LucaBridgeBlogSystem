package com.lucabridge.core.service;

import com.lucabridge.core.service.dto.AdminServiceDto;
import com.lucabridge.core.service.dto.ServiceUpsertRequest;
import com.lucabridge.core.service.dto.ServiceUsageDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every route here is under /api/admin/services/**, which SecurityConfig restricts to
 * ADMIN/EDITOR (DELETE further narrowed to ADMIN-only). No created_by/updated_by on this table
 * — it's a bare reference table — so no CurrentUser dependency here.
 */
@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<AdminServiceDto> list() {
        return serviceService.listAll().stream().map(ServiceMapper::toAdminDto).toList();
    }

    @GetMapping("/{id}")
    public AdminServiceDto get(@PathVariable Long id) {
        return ServiceMapper.toAdminDto(serviceService.get(id));
    }

    /** What the delete confirmation dialog shows before removing the chip from live content. */
    @GetMapping("/{id}/usage")
    public ServiceUsageDto usage(@PathVariable Long id) {
        var usage = serviceService.usage(id);
        return new ServiceUsageDto(usage.blogCount(), usage.eventCount());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminServiceDto create(@Valid @RequestBody ServiceUpsertRequest request) {
        return ServiceMapper.toAdminDto(serviceService.create(request));
    }

    @PutMapping("/{id}")
    public AdminServiceDto update(@PathVariable Long id, @Valid @RequestBody ServiceUpsertRequest request) {
        return ServiceMapper.toAdminDto(serviceService.update(id, request));
    }

    /** Refuses with 409 (reference counts in the message) unless confirm=true when the service is still in use — see ServiceService.delete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm) {
        serviceService.delete(id, confirm);
    }
}
