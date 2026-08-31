package com.lucabridge.core;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.i18n.Localized;
import com.lucabridge.core.publish.PublishStatus;
import com.lucabridge.core.publish.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Everything above the repository layer depends on these two pieces, so they are pinned by
 * tests before anything is built on top of them.
 */
class CoreFoundationTest {

    private static final Instant NOW = Instant.parse("2026-08-30T12:00:00Z");
    private static final Instant PAST = NOW.minusSeconds(3600);
    private static final Instant FUTURE = NOW.plusSeconds(3600);
    private static final PublishStatus PUB = PublishStatus.PUBLISHED;

    @Nested
    @DisplayName("Lang")
    class LangTest {

        @Test
        void acceptsInternalTokens() {
            assertEquals(Lang.TC, Lang.orDefault("tc"));
            assertEquals(Lang.EN, Lang.orDefault("en"));
            assertEquals(Lang.SC, Lang.orDefault("sc"));
        }

        @Test
        @DisplayName("still accepts the pre-migration /zh-Hant and /zh-Hans path segments")
        void acceptsLegacySegments() {
            assertEquals(Lang.TC, Lang.orDefault("zh-Hant"));
            assertEquals(Lang.SC, Lang.orDefault("zh-Hans"));
            assertEquals(Lang.TC, Lang.orDefault("ZH-HANT"));
            assertEquals(Lang.SC, Lang.orDefault("zh_Hans"));
            assertEquals(Lang.SC, Lang.orDefault("zh-CN"));
            assertEquals(Lang.EN, Lang.orDefault("en-GB"));
        }

        @Test
        void unknownAndNullFallBackToBaseLanguage() {
            assertEquals(Lang.TC, Lang.orDefault("fr"));
            assertEquals(Lang.TC, Lang.orDefault(null));
            assertEquals(Lang.TC, Lang.DEFAULT);
        }

        @Test
        void strictParseRejectsUnknown() {
            assertThrows(IllegalArgumentException.class, () -> Lang.of("klingon"));
        }

        @Test
        @DisplayName("the short token never doubles as a language tag")
        void bcp47IsDistinctFromCode() {
            assertEquals("tc", Lang.TC.code());
            assertEquals("zh-Hant", Lang.TC.bcp47());
            assertEquals("zh-Hans", Lang.SC.bcp47());
            assertEquals("en", Lang.EN.bcp47());
            assertEquals("sc_", Lang.SC.columnPrefix());
        }
    }

    @Nested
    @DisplayName("Localized fallback")
    class LocalizedTest {

        @Test
        void returnsRequestedLanguageWhenPresent() {
            assertEquals("English", Localized.pick(Lang.EN, "繁", "English", "简"));
            assertEquals("简", Localized.pick(Lang.SC, "繁", "English", "简"));
            assertEquals("繁", Localized.pick(Lang.TC, "繁", "English", "简"));
        }

        @Test
        @DisplayName("a blank translation falls back rather than rendering an empty heading")
        void blankCountsAsMissing() {
            assertEquals("繁", Localized.pick(Lang.EN, "繁", null, "简"));
            assertEquals("繁", Localized.pick(Lang.EN, "繁", "", "简"));
            assertEquals("繁", Localized.pick(Lang.EN, "繁", "   ", "简"));
            assertEquals("繁", Localized.pick(Lang.SC, "繁", "English", null));
        }

        @Test
        void isTranslatedReportsTheTruthNotTheFallback() {
            assertFalse(Localized.isTranslated(Lang.EN, "繁", null, "简"));
            assertFalse(Localized.isTranslated(Lang.EN, "繁", "  ", "简"));
            assertTrue(Localized.isTranslated(Lang.EN, "繁", "English", "简"));
            assertTrue(Localized.isTranslated(Lang.TC, "繁", null, null));
        }
    }

    @Nested
    @DisplayName("Visibility")
    class VisibilityTest {

        @Test
        void publishedWithNoWindowIsVisible() {
            assertTrue(Visibility.isVisible(PUB, null, null, null, NOW));
        }

        @Test
        void draftAndArchivedAreNeverVisible() {
            assertFalse(Visibility.isVisible(PublishStatus.DRAFT, null, null, null, NOW));
            assertFalse(Visibility.isVisible(PublishStatus.ARCHIVED, null, null, null, NOW));
        }

        @Test
        @DisplayName("status beats the window: an open window cannot publish a draft")
        void statusOverridesWindow() {
            assertFalse(Visibility.isVisible(PublishStatus.DRAFT, PAST, FUTURE, null, NOW));
        }

        @Test
        void softDeletedIsNeverVisible() {
            assertFalse(Visibility.isVisible(PUB, null, null, PAST, NOW));
        }

        @Test
        void windowIsHonoured() {
            assertFalse(Visibility.isVisible(PUB, FUTURE, null, null, NOW));
            assertTrue(Visibility.isVisible(PUB, PAST, null, null, NOW));
            assertTrue(Visibility.isVisible(PUB, null, FUTURE, null, NOW));
            assertFalse(Visibility.isVisible(PUB, null, PAST, null, NOW));
            assertTrue(Visibility.isVisible(PUB, PAST, FUTURE, null, NOW));
        }

        @Test
        @DisplayName("publishAt is inclusive, unpublishAt is exclusive")
        void boundariesAreExact() {
            assertTrue(Visibility.isVisible(PUB, NOW, null, null, NOW));
            assertFalse(Visibility.isVisible(PUB, null, NOW, null, NOW));
        }

        @Test
        @DisplayName("a job past its closing date drops off without anyone unpublishing it")
        void jobClosingDateIsEnforced() {
            assertTrue(Visibility.isJobVisible(PUB, null, null, FUTURE, null, NOW));
            assertTrue(Visibility.isJobVisible(PUB, null, null, null, null, NOW));
            assertFalse(Visibility.isJobVisible(PUB, null, null, PAST, null, NOW));
        }

        @Test
        @DisplayName("the CMS badge never says Published for something that is not on the site")
        void cmsStateMatchesReality() {
            assertEquals(Visibility.State.DRAFT,
                    Visibility.stateOf(PublishStatus.DRAFT, null, null, null, NOW));
            assertEquals(Visibility.State.SCHEDULED,
                    Visibility.stateOf(PUB, FUTURE, null, null, NOW));
            assertEquals(Visibility.State.LIVE,
                    Visibility.stateOf(PUB, PAST, FUTURE, null, NOW));
            assertEquals(Visibility.State.EXPIRED,
                    Visibility.stateOf(PUB, PAST, PAST, null, NOW));
            assertEquals(Visibility.State.ARCHIVED,
                    Visibility.stateOf(PublishStatus.ARCHIVED, null, null, null, NOW));
            assertEquals(Visibility.State.DELETED,
                    Visibility.stateOf(PUB, null, null, PAST, NOW));
        }

        @Test
        @DisplayName("a job past closes_at is EXPIRED even with an open publish window, since JPQL_JOB has already dropped it")
        void jobPastClosingDateIsExpiredEvenWhileLive() {
            assertEquals(Visibility.State.EXPIRED,
                    Visibility.stateOfJob(PUB, PAST, FUTURE, PAST, null, NOW));
            assertEquals(Visibility.State.LIVE,
                    Visibility.stateOfJob(PUB, PAST, FUTURE, FUTURE, null, NOW));
            assertEquals(Visibility.State.LIVE,
                    Visibility.stateOfJob(PUB, PAST, FUTURE, null, null, NOW));
        }

        @Test
        @DisplayName("closesAt never turns a non-LIVE state into EXPIRED")
        void closesAtOnlyDowngradesLive() {
            assertEquals(Visibility.State.DRAFT,
                    Visibility.stateOfJob(PublishStatus.DRAFT, null, null, PAST, null, NOW));
            assertEquals(Visibility.State.SCHEDULED,
                    Visibility.stateOfJob(PUB, FUTURE, null, PAST, null, NOW));
        }

        @Test
        @DisplayName("home_block has no status/deletedAt, just is_active plus the window")
        void activeVisibilityHasNoStatusOrSoftDelete() {
            assertTrue(Visibility.isActiveVisible(true, null, null, NOW));
            assertFalse(Visibility.isActiveVisible(false, null, null, NOW));
            assertFalse(Visibility.isActiveVisible(true, FUTURE, null, NOW));
            assertTrue(Visibility.isActiveVisible(true, PAST, FUTURE, NOW));
            assertFalse(Visibility.isActiveVisible(true, null, PAST, NOW));
        }
    }
}
