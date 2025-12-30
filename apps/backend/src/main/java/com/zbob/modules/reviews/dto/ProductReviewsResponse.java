package com.zbob.modules.reviews.dto;

import com.zbob.common.dto.PageResponse;

/** Approved reviews for one product. The score and count come from approved reviews only. */
public record ProductReviewsResponse(double averageRating, long reviewCount, PageResponse<PublicReviewResponse> reviews) {
}
