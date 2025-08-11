package com.zbob.modules.reviews.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotBlank @Size(max = 120) String authorName,
        @Size(max = 120) String authorTitle,
        @NotBlank @Size(max = 200) String headline,
        @NotBlank @Size(max = 2000) String body,
        @Min(1) @Max(5) int rating,
        boolean approved) {
}
