package com.zbob.modules.blog.dto;

import java.time.Instant;

public record BlogPostSummaryResponse(
        Long id, String title, String slug, String excerpt, String topic, int readMinutes, String coverUrl, Instant publishedAt) {
}
