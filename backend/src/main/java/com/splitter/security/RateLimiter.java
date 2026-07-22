package com.splitter.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple fixed-window rate limiter: each key (e.g. "ip:endpoint") gets a
 * counter that resets once its window expires. Not as smooth as a token-bucket
 * (bursts right at a window boundary are technically allowed twice), but it's
 * dependency-free, easy to reason about, and more than sufficient for
 * protecting auth endpoints from brute-force/spam abuse.
 */
@Component
public class RateLimiter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    /** Returns true if this request is allowed, false if the key has hit its limit. */
    public boolean tryConsume(String key, int maxAttempts, Duration windowDuration) {
        long now = System.currentTimeMillis();
        long windowMillis = windowDuration.toMillis();

        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= windowMillis) {
                return new Window(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return window.count.get() <= maxAttempts;
    }

    /** How many seconds until this key's current window resets — used for a Retry-After header. */
    public long secondsUntilReset(String key, Duration windowDuration) {
        Window window = windows.get(key);
        if (window == null) return 0;
        long elapsed = System.currentTimeMillis() - window.windowStart;
        long remaining = windowDuration.toMillis() - elapsed;
        return Math.max(0, remaining / 1000);
    }

    /** Prevents unbounded memory growth from IPs that only ever hit the endpoint once. */
    @Scheduled(fixedRate = 3600_000) // hourly
    public void cleanupStaleWindows() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > 3600_000);
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(1);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}