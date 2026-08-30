package com.lucabridge.core.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * The three languages the site serves.
 *
 * <p>{@link #code()} is the short internal token used in URLs, API parameters, database
 * column prefixes and stored preferences. {@link #bcp47()} is the real language tag and is
 * the ONLY form that may reach a browser or a crawler: {@code <html lang>}, {@code hreflang}
 * and sitemap alternates. {@code lang="tc"} is not a valid language tag and Google silently
 * ignores invalid hreflang, which would cost the multilingual SEO the SSR migration exists for.
 */
public enum Lang {

    /** 繁體中文 — the base language. Every record must have tc_* text; the others fall back to it. */
    TC("tc", "zh-Hant", "zh_HK"),
    EN("en", "en", "en_US"),
    SC("sc", "zh-Hans", "zh_CN");

    /** Fallback for a missing translation and for an unrecognised request. */
    public static final Lang DEFAULT = TC;

    private final String code;
    private final String bcp47;
    private final String openGraphLocale;

    Lang(String code, String bcp47, String openGraphLocale) {
        this.code = code;
        this.bcp47 = bcp47;
        this.openGraphLocale = openGraphLocale;
    }

    /** Internal token: {@code tc}, {@code en}, {@code sc}. URLs, API params, column prefixes. */
    public String code() {
        return code;
    }

    /** BCP-47 tag for {@code <html lang>}, {@code hreflang} and sitemap alternates. */
    public String bcp47() {
        return bcp47;
    }

    /** Value for the {@code og:locale} meta tag. */
    public String openGraphLocale() {
        return openGraphLocale;
    }

    /** Column prefix for this language, e.g. {@code "tc_"}. */
    public String columnPrefix() {
        return code + "_";
    }

    /**
     * Lenient parse. Accepts the internal token, the BCP-47 tag, and the legacy path segments
     * the site used before the {@code /tc /sc /en} move, so old inbound links still resolve
     * while the 301s do their work. Case and separator insensitive.
     */
    public static Optional<Lang> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String v = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        return switch (v) {
            case "tc", "zh-hant", "zh-hk", "zh-tw", "zh-mo", "zh" -> Optional.of(TC);
            case "sc", "zh-hans", "zh-cn", "zh-sg" -> Optional.of(SC);
            case "en", "en-us", "en-gb", "en-hk", "en-au" -> Optional.of(EN);
            default -> Optional.empty();
        };
    }

    /** Parse, falling back to {@link #DEFAULT} for anything unrecognised or absent. */
    public static Lang orDefault(String raw) {
        return parse(raw).orElse(DEFAULT);
    }

    /** Strict parse for places where an unknown language must be an error, not a silent default. */
    public static Lang of(String raw) {
        return parse(raw).orElseThrow(
                () -> new IllegalArgumentException("Unsupported language: " + raw));
    }
}
