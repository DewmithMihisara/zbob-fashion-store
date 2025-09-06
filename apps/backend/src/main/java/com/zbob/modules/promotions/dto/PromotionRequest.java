package com.zbob.modules.promotions.dto;

import com.zbob.modules.promotions.entity.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record PromotionRequest(
        @NotBlank @Size(max = 40) @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Use only letters, numbers, - and _") String code,
        @Size(max = 200) String description,
        @NotNull DiscountType discountType,
        @Min(1) int discountValue,
        @Min(0) int minimumCents,
        Instant startsAt,
        Instant endsAt,
        boolean active) {
}
