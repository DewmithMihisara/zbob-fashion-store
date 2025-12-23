package com.zbob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** provider is "log" (write emails to the log) or "smtp" (send them). storefrontUrl is used in email links. */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        @DefaultValue("log") String provider,
        @DefaultValue("zbob <hello@zbob.example>") String from,
        @DefaultValue("http://localhost:3000") String storefrontUrl) {
}
