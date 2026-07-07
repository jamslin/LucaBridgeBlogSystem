package com.lucabridge.blog.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal in-memory fixed-window rate limiter for the login endpoint.
 * Single-node deployment (see 03 · Architecture), so in-memory is sufficient;
 * swap for a Redis-backed limiter if the app ever runs on multiple nodes.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS_PER_WINDOW = 10;
    static final Duration WINDOW = Duration.ofMinutes(5);

    private record Window(Instant start, AtomicInteger count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    /** Returns true if this attempt is allowed for the given client key (IP). */
    public boolean tryAcquire(String key) {
        Instant now = Instant.now();
        cleanUp(now);
        Window window = windows.compute(key, (k, w) ->
                (w == null || now.isAfter(w.start().plus(WINDOW)))
                        ? new Window(now, new AtomicInteger(0))
                        : w);
        return window.count().incrementAndGet() <= MAX_ATTEMPTS_PER_WINDOW;
    }

    private void cleanUp(Instant now) {
        if (windows.size() < 1000) return; // only bother under memory pressure
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            if (now.isAfter(it.next().getValue().start().plus(WINDOW))) {
                it.remove();
            }
        }
    }
}
