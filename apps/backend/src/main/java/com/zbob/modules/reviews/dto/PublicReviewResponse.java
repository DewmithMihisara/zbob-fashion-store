package com.zbob.modules.reviews.dto;

import java.time.Instant;

public record PublicReviewResponse(Long id, String authorName, String headline, String body, int rating, Instant createdAt) {
}
