package com.zbob.modules.promotions.dto;

import com.zbob.modules.promotions.entity.DiscountType;
import java.time.Instant;

public record PromotionResponse(
        Long id, String code, String description, DiscountType discountType, int discountValue, int minimumCents,
        Instant startsAt, Instant endsAt, boolean active) {
}
