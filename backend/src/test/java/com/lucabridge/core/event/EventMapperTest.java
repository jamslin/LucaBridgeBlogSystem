package com.lucabridge.core.event;

import com.lucabridge.core.event.dto.EventSummaryDto;
import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.publish.PublishStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** almostFull ("即將額滿") fires at remaining <= 20% of capacity — the design's own boundary. */
class EventMapperTest {

    private static final Instant NOW = Instant.parse("2026-08-30T12:00:00Z");

    @Test
    @DisplayName("remaining at exactly 20% of capacity is almost full")
    void almostFullAtExactlyTwentyPercent() {
        // capacity 10, confirmed 8 -> remaining 2 -> 2/10 == 20%
        EventSummaryDto dto = EventMapper.toSummary(openEvent(10), Lang.TC, 8, NOW);
        assertTrue(dto.registration().almostFull());
    }

    @Test
    @DisplayName("remaining just above 20% of capacity is not almost full")
    void notAlmostFullJustAboveTwentyPercent() {
        // capacity 50, confirmed 39 -> remaining 11 -> 22%, matches the design's own example card
        EventSummaryDto dto = EventMapper.toSummary(openEvent(50), Lang.TC, 39, NOW);
        assertFalse(dto.registration().almostFull());
    }

    @Test
    @DisplayName("a full event is FULL, not almost-full — the badges are mutually exclusive")
    void fullEventIsNeverAlmostFull() {
        EventSummaryDto dto = EventMapper.toSummary(openEvent(10), Lang.TC, 10, NOW);
        assertFalse(dto.registration().almostFull());
        assertTrue(dto.registration().state() == RegistrationState.FULL);
    }

    @Test
    @DisplayName("unlimited capacity is never almost full")
    void unlimitedCapacityIsNeverAlmostFull() {
        Event event = openEvent(null);
        EventSummaryDto dto = EventMapper.toSummary(event, Lang.TC, 1_000_000, NOW);
        assertFalse(dto.registration().almostFull());
    }

    private static Event openEvent(Integer capacity) {
        Event event = Event.builder()
                .status(PublishStatus.PUBLISHED)
                .registerable(true)
                .registrationOpensAt(NOW.minusSeconds(3600))
                .registrationClosesAt(NOW.plusSeconds(3600))
                .capacity(capacity)
                .build();
        EventText text = EventText.builder().tcTitle("測試").build();
        event.setText(text);
        return event;
    }
}
