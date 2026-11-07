package com.zbob.modules.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogPostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String excerpt,
        @NotBlank String body,
        @NotBlank @Size(max = 40) String topic,
        @Min(1) @Max(120) int readMinutes,
        boolean published) {
}
