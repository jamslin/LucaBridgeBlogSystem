package com.lucabridge.core.event;

import com.lucabridge.core.event.dto.EventRegistrationRequest;
import com.lucabridge.core.event.dto.EventRegistrationResponse;
import com.lucabridge.core.publish.PublishStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Everything RegistrationStateTest and EventMapperTest pin is pure-function logic, testable
 * against in-memory values. These two properties are not: they depend on what the database
 * actually does under real concurrent writes, so they need a real Postgres, not a mock.
 *
 * <p>Runs on {@code mvn verify} (Failsafe), not {@code mvn test} — needs Docker for
 * Testcontainers, unlike every other test in this project.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class EventRegistrationConcurrencyIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRegistrationService registrationService;

    private Long eventId;

    @BeforeEach
    void createOpenEventWithCapacityTwo() {
        Event event = Event.builder()
                .slug("concurrency-it-" + UUID.randomUUID())
                .status(PublishStatus.PUBLISHED)
                .registerable(true)
                .registrationOpensAt(Instant.now().minusSeconds(3600))
                .registrationClosesAt(Instant.now().plusSeconds(3600))
                .capacity(2)
                .build();
        EventText text = EventText.builder().tcTitle("併發測試").build();
        event.setText(text);
        text.setEvent(event);
        eventId = eventRepository.save(event).getId();
    }

    /**
     * The property the pessimistic lock in EventRepository.findByIdForUpdate exists for: six
     * requests racing for two places must never let more than two through as CONFIRMED. Proved
     * once by hand with curl (4 concurrent requests, capacity 2 -> exactly 2 CONFIRMED + 2
     * WAITLIST); this is that same proof, automated, with a wider margin (6 requests) and a
     * real thread-pool race rather than four sequential shell processes.
     */
    @Test
    @DisplayName("capacity 2, 6 concurrent submissions -> exactly 2 CONFIRMED, the rest WAITLIST, never more than 2 CONFIRMED")
    void concurrentSubmissionsNeverExceedCapacity() throws Exception {
        int attempts = 6;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch allReady = new CountDownLatch(attempts);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<RegistrationStatus>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            int idx = i;
            futures.add(pool.submit(() -> {
                allReady.countDown();
                go.await();
                EventRegistrationResponse response = registrationService.submit(eventId, request(idx));
                return response.status();
            }));
        }

        allReady.await(10, TimeUnit.SECONDS);
        go.countDown(); // release all six threads at once, maximising the chance of a genuine race

        AtomicLong confirmed = new AtomicLong();
        AtomicLong waitlist = new AtomicLong();
        for (Future<RegistrationStatus> future : futures) {
            RegistrationStatus status = future.get(15, TimeUnit.SECONDS);
            if (status == RegistrationStatus.CONFIRMED) {
                confirmed.incrementAndGet();
            } else if (status == RegistrationStatus.WAITLIST) {
                waitlist.incrementAndGet();
            }
        }
        pool.shutdown();

        assertEquals(2, confirmed.get(), "exactly capacity's worth of submissions should be CONFIRMED");
        assertEquals(attempts - 2, waitlist.get(), "everyone else should be WAITLIST, not rejected or lost");

        long actuallyConfirmedInDb = registrationRepository.countByEventIdAndStatusIn(
                eventId, RegistrationStatus.OCCUPIES_CAPACITY);
        assertEquals(2, actuallyConfirmedInDb, "the database itself must agree - not just the in-memory responses");
    }

    /**
     * ATTENDED must occupy capacity the same as CONFIRMED (RegistrationStatus.OCCUPIES_CAPACITY),
     * checked here through the real countByEventIdAndStatusIn query and the real submit() path,
     * not just the static Set membership RegistrationStatusTest already covers.
     */
    @Test
    @DisplayName("an ATTENDED registration still occupies its place - a new submission still lands on WAITLIST, not the freed-looking spot")
    void attendedRegistrationStillOccupiesCapacity() {
        RegistrationStatus first = registrationService.submit(eventId, request(1)).status();
        RegistrationStatus second = registrationService.submit(eventId, request(2)).status();
        assertEquals(RegistrationStatus.CONFIRMED, first);
        assertEquals(RegistrationStatus.CONFIRMED, second);

        EventRegistration toMarkAttended = registrationRepository.findByEventIdOrderBySubmittedAtAsc(eventId).get(0);
        toMarkAttended.setStatus(RegistrationStatus.ATTENDED);
        registrationRepository.saveAndFlush(toMarkAttended);

        RegistrationStatus third = registrationService.submit(eventId, request(3)).status();
        assertEquals(RegistrationStatus.WAITLIST, third);

        long occupied = registrationRepository.countByEventIdAndStatusIn(eventId, RegistrationStatus.OCCUPIES_CAPACITY);
        assertEquals(2, occupied, "one CONFIRMED + one ATTENDED = 2 occupied places, not 1");
    }

    private EventRegistrationRequest request(int idx) {
        return new EventRegistrationRequest(
                "Concurrency Tester " + idx,
                null,
                null,
                "concurrency-it-" + idx + "-" + UUID.randomUUID() + "@example.com",
                "9000" + String.format("%04d", idx),
                null,
                null,
                null,
                "tc",
                "v1",
                "v1",
                false);
    }
}
