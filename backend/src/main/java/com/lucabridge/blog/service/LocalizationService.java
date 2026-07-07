package com.lucabridge.blog.service;

import com.lucabridge.blog.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Central place for the localization fallback rule described in
 * Notion "03 · Architecture, Data Model & API":
 *
 *   Every public read takes `lang` (default 繁中 / zh-Hant). Missing translation
 *   falls back to zh-Hant and the response sets `fallback: true`.
 */
@Service
public class LocalizationService {

    private final AppProperties appProperties;

    public LocalizationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String defaultLang() {
        return appProperties.getI18n().getDefaultLang();
    }

    public boolean isSupported(String lang) {
        return lang != null && appProperties.getI18n().getSupportedLangs().contains(lang);
    }

    /** Normalizes a requested lang query param, falling back to the default if unsupported/absent. */
    public String normalize(String requestedLang) {
        return isSupported(requestedLang) ? requestedLang : defaultLang();
    }

    public record Resolved<T>(T value, boolean fallback) {}

    /**
     * Picks the translation matching {@code lang} out of {@code translations}; if none exists,
     * falls back to the default language's translation and marks {@code fallback = true}.
     */
    public <T> Optional<Resolved<T>> resolve(List<T> translations, String lang, Function<T, String> langOf) {
        Optional<T> exact = translations.stream().filter(t -> langOf.apply(t).equals(lang)).findFirst();
        if (exact.isPresent()) {
            return Optional.of(new Resolved<>(exact.get(), false));
        }
        return translations.stream()
                .filter(t -> langOf.apply(t).equals(defaultLang()))
                .findFirst()
                .map(t -> new Resolved<>(t, true));
    }
}
