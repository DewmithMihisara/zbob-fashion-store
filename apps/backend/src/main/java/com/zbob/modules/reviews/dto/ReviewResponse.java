package com.zbob.modules.reviews.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id, String authorName, String authorTitle, String headline, String body, int rating, boolean approved,
        Long productId, String productName, Instant createdAt) {
}
