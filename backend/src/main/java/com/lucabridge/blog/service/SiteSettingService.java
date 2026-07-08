package com.lucabridge.blog.service;

import com.lucabridge.blog.entity.SiteSetting;
import com.lucabridge.blog.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAll() {
        Map<String, String> settings = new LinkedHashMap<>();
        for (SiteSetting s : siteSettingRepository.findAll()) {
            settings.put(s.getKey(), s.getValue());
        }
        return settings;
    }
}
