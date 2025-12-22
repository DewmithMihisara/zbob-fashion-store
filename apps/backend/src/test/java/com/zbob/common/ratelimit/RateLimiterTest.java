package com.zbob.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    /** A clock the test moves by hand. */
    static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final TestClock clock = new TestClock();
    private final RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(10), clock);

    @Test
    void allowsUpToTheLimitThenBlocks() {
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire("a")).isZero();
        }
        assertThat(limiter.tryAcquire("a")).isEqualTo(600);
    }

    @Test
    void waitShrinksAsTimePasses() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a");
        }
        clock.advance(Duration.ofMinutes(4));

        assertThat(limiter.secondsUntilAllowed("a")).isEqualTo(360);
    }

    @Test
    void attemptsExpireOutOfTheWindow() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a");
        }
        clock.advance(Duration.ofMinutes(10));

        assertThat(limiter.tryAcquire("a")).isZero();
    }

    @Test
    void keysAreCountedSeparately() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a");
        }

        assertThat(limiter.tryAcquire("b")).isZero();
    }

    @Test
    void clearingResetsAKey() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a");
        }
        limiter.clear("a");

        assertThat(limiter.tryAcquire("a")).isZero();
    }

    @Test
    void blockedAttemptsDoNotExtendTheWait() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a");
        }
        clock.advance(Duration.ofMinutes(5));
        limiter.tryAcquire("a");

        assertThat(limiter.secondsUntilAllowed("a")).isEqualTo(300);
    }

    @Test
    void refusesAZeroLimit() {
        assertThatThrownBy(() -> new RateLimiter(0, Duration.ofMinutes(1), clock)).isInstanceOf(IllegalArgumentException.class);
    }
}
