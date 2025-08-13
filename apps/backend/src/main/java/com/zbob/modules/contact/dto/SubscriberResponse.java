package com.zbob.modules.contact.dto;

import java.time.Instant;

public record SubscriberResponse(Long id, String email, Instant subscribedAt) {
}
