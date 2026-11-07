package com.zbob.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Admin admin, Storage storage) {

    public record Jwt(String secret, Duration expiry) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** Where uploaded images go: provider is "local" or "gcs". */
    public record Storage(String provider, String localDir, String publicBaseUrl, String bucket) {
    }

    /** First admin account, created at startup when both values are set. */
    public record Admin(String email, String password) {
    }
}
