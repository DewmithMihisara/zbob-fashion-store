package com.zbob.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Admin admin) {

    public record Jwt(String secret, Duration expiry) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** First admin account, created at startup when both values are set. */
    public record Admin(String email, String password) {
    }
}
