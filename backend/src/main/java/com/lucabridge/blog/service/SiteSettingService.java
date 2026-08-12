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

    /** Upsert each key/value. A null value clears the setting to an empty string. */
    @Transactional
    public Map<String, String> upsert(Map<String, String> updates) {
        for (Map.Entry<String, String> e : updates.entrySet()) {
            String key = e.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            SiteSetting setting = siteSettingRepository.findById(key)
                    .orElseGet(() -> SiteSetting.builder().key(key).build());
            setting.setValue(e.getValue() != null ? e.getValue() : "");
            siteSettingRepository.save(setting);
        }
        return getAll();
    }
}
