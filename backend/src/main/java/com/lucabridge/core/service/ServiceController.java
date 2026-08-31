package com.lucabridge.core.service;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.service.dto.ServiceDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public read API — active services only, in sort_order. A small closed list, so a plain List, not a Page. */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceDto> list(@RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return serviceService.listActive().stream().map(s -> ServiceMapper.toDto(s, lang)).toList();
    }
}
