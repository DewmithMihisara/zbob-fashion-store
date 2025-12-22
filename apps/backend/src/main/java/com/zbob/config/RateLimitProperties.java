package com.zbob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * How many attempts each source may make. Login counts failures per 15 minutes, sign up and password
 * reset count requests per hour, and the contact and newsletter forms count submissions per 10 minutes.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("5") int loginPerEmail,
        @DefaultValue("50") int loginPerIp,
        @DefaultValue("10") int signupPerIp,
        @DefaultValue("3") int resetPerEmail,
        @DefaultValue("20") int resetPerIp,
        @DefaultValue("10") int formsPerIp) {
}
