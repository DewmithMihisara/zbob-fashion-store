package com.zbob.modules.blog.dto;

import java.time.Instant;

public record BlogPostResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String body,
        String topic,
        int readMinutes,
        String coverUrl,
        Instant publishedAt) {
}
