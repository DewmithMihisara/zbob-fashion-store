package com.zbob.modules.reviews.dto;

public record ReviewResponse(
        Long id, String authorName, String authorTitle, String headline, String body, int rating, boolean approved) {
}
