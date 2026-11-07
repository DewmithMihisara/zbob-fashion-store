package com.zbob.common.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows a number of attempts per key within a sliding time window. State lives in memory, so each
 * server instance counts separately.
 */
public class RateLimiter {

    /** Stops a flood of distinct keys from growing memory without bound. */
    private static final int MAX_KEYS = 50_000;

    private final int limit;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> hits = new HashMap<>();

    public RateLimiter(int limit, Duration window, Clock clock) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        this.limit = limit;
        this.window = window;
        this.clock = clock;
    }

    /** Seconds until another attempt is allowed for this key, or 0 when one is allowed now. */
    public synchronized long secondsUntilAllowed(String key) {
        Deque<Instant> times = prune(key);
        if (times == null || times.size() < limit) {
            return 0;
        }
        long millis = Duration.between(clock.instant(), times.peekFirst().plus(window)).toMillis();
        return Math.max(1, (millis + 999) / 1000);
    }

    public synchronized void record(String key) {
        if (hits.size() >= MAX_KEYS && !hits.containsKey(key)) {
            evictExpired();
        }
        Deque<Instant> times = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        times.addLast(clock.instant());
        // Only the latest attempts up to the limit matter, so older ones can go.
        while (times.size() > limit) {
            times.pollFirst();
        }
    }

    /** Checks and counts in one step. Returns the seconds to wait, or 0 when the attempt was allowed. */
    public synchronized long tryAcquire(String key) {
        long wait = secondsUntilAllowed(key);
        if (wait == 0) {
            record(key);
        }
        return wait;
    }

    public synchronized void clear(String key) {
        hits.remove(key);
    }

    private Deque<Instant> prune(String key) {
        Deque<Instant> times = hits.get(key);
        if (times == null) {
            return null;
        }
        Instant cutoff = clock.instant().minus(window);
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.pollFirst();
        }
        if (times.isEmpty()) {
            hits.remove(key);
            return null;
        }
        return times;
    }

    private void evictExpired() {
        new ArrayList<>(hits.keySet()).forEach(this::prune);
        if (hits.size() >= MAX_KEYS) {
            hits.clear();
        }
    }
}
