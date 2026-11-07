package com.zbob.common.ratelimit;

import com.zbob.config.RateLimitProperties;
import com.zbob.exception.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class RateLimits {

    private final RateLimiter loginPerEmail;
    private final RateLimiter loginPerIp;
    private final RateLimiter signupPerIp;
    private final RateLimiter resetPerEmail;
    private final RateLimiter resetPerIp;
    private final RateLimiter formsPerIp;

    public RateLimits(RateLimitProperties p) {
        Clock clock = Clock.systemUTC();
        Duration loginWindow = Duration.ofMinutes(15);
        this.loginPerEmail = new RateLimiter(p.loginPerEmail(), loginWindow, clock);
        this.loginPerIp = new RateLimiter(p.loginPerIp(), loginWindow, clock);
        this.signupPerIp = new RateLimiter(p.signupPerIp(), Duration.ofHours(1), clock);
        this.resetPerEmail = new RateLimiter(p.resetPerEmail(), Duration.ofHours(1), clock);
        this.resetPerIp = new RateLimiter(p.resetPerIp(), Duration.ofHours(1), clock);
        this.formsPerIp = new RateLimiter(p.formsPerIp(), Duration.ofMinutes(10), clock);
    }

    /** Throws when the wait is above zero. */
    public static void enforce(long waitSeconds) {
        if (waitSeconds > 0) {
            throw new TooManyRequestsException(waitSeconds);
        }
    }

    public RateLimiter loginPerEmail() {
        return loginPerEmail;
    }

    public RateLimiter loginPerIp() {
        return loginPerIp;
    }

    public RateLimiter signupPerIp() {
        return signupPerIp;
    }

    public RateLimiter resetPerEmail() {
        return resetPerEmail;
    }

    public RateLimiter resetPerIp() {
        return resetPerIp;
    }

    public RateLimiter formsPerIp() {
        return formsPerIp;
    }
}
