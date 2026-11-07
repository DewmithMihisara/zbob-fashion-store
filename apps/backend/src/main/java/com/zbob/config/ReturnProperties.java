package com.zbob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** How many days after delivery a customer may ask to return an order. Set it to match your returns policy. */
@ConfigurationProperties(prefix = "app.returns")
public record ReturnProperties(@DefaultValue("30") int windowDays) {
}
