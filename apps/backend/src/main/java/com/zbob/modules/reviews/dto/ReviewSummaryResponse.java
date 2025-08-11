package com.zbob.modules.reviews.dto;

import java.util.List;

/** Home page block. The score and count are computed from approved reviews only. */
public record ReviewSummaryResponse(double averageRating, long reviewCount, List<ReviewResponse> reviews) {
}
