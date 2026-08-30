package com.lucabridge.core.i18n;

/**
 * The single place translation fallback happens.
 *
 * <p>Translatable text is stored as flattened {@code tc_/en_/sc_} columns, with {@code tc_*}
 * NOT NULL and the other two optional. Every read must resolve the requested language and fall
 * back to 繁中 when that language is missing.
 *
 * <p>This class exists so that logic lives in exactly one place. Scattering the fallback across
 * DTO mappers is how one of them eventually forgets it, and the failure mode is not an
 * exception — it is a blank English title on the live site that nobody notices for a month.
 *
 * <p>{@link #pick} takes all three values by design: you cannot call it having forgotten one.
 */
public final class Localized {

    private Localized() {
    }

    /**
     * Resolve one field. Returns the value for {@code lang}, or the 繁中 value when that
     * language is absent. A null or blank translation counts as absent — an empty string in
     * {@code en_title} must fall back, not render as an empty heading.
     */
    public static String pick(Lang lang, String tc, String en, String sc) {
        String requested = switch (lang) {
            case TC -> tc;
            case EN -> en;
            case SC -> sc;
        };
        return hasText(requested) ? requested : tc;
    }

    /**
     * Whether this field is genuinely translated into {@code lang}, as opposed to falling back.
     * Drives the CMS "which languages are still missing" view; never used for rendering.
     */
    public static boolean isTranslated(Lang lang, String tc, String en, String sc) {
        return hasText(switch (lang) {
            case TC -> tc;
            case EN -> en;
            case SC -> sc;
        });
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
