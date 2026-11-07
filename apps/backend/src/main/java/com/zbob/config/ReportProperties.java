package com.zbob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** The time zone that decides which day an order counts towards, for example Asia/Colombo. */
@ConfigurationProperties(prefix = "app.reports")
public record ReportProperties(@DefaultValue("UTC") String timeZone) {
}
