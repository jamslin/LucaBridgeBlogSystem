package com.lucabridge.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;

/**
 * Resolves a {@code serviceId} to the localised service name that article and event cards
 * show as their tag (design pass 1: 活動卡與文章卡都帶服務標籤).
 *
 * <p>Blog and Event hold {@code serviceId} as a plain column rather than an association, so
 * there is nothing to JOIN FETCH. Listing endpoints therefore take the whole (small, rarely
 * changing) table once per request instead of issuing a lookup per row.
 *
 * <p>Inactive services are included on purpose: retiring a service must not strip the tag off
 * the articles already published under it.
 */
@Component
public class ServiceLabels {

    private final ServiceRepository repository;

    public ServiceLabels(ServiceRepository repository) {
        this.repository = repository;
    }

    /** Service id to localised name. Ids whose name is missing entirely are simply absent. */
    @Transactional(readOnly = true)
    public Map<Long, String> namesBy(Lang lang) {
        List<Service> all = repository.findAllForAdmin();
        Map<Long, String> names = new HashMap<>(all.size());
        for (Service s : all) {
            ServiceText text = s.getText();
            if (text == null) {
                continue;
            }
            String name = Localized.pick(lang, text.getTcName(), text.getEnName(), text.getScName());
            if (name != null && !name.isBlank()) {
                names.put(s.getId(), name);
            }
        }
        return names;
    }

    /** Single-record convenience for the detail endpoints. Null id or unknown id yields null. */
    @Transactional(readOnly = true)
    public String nameOf(Long serviceId, Lang lang) {
        return serviceId == null ? null : namesBy(lang).get(serviceId);
    }
}
