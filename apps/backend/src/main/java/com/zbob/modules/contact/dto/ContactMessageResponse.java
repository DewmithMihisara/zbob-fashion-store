package com.zbob.modules.contact.dto;

import java.time.Instant;

public record ContactMessageResponse(
        Long id, String name, String email, String subject, String message, boolean handled, Instant receivedAt) {
}
