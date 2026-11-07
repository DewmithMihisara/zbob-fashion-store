package com.zbob.modules.reviews.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerReviewRequest(
        @NotNull Long productId,
        @NotBlank @Size(max = 200) String headline,
        @NotBlank @Size(max = 2000) String body,
        @Min(1) @Max(5) int rating) {
}
