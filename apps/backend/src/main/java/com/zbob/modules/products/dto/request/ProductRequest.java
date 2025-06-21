package com.zbob.modules.products.dto.request;

import com.zbob.modules.products.entity.ProductBadge;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 4000) String description,
        @Size(max = 500) String material,
        @Size(max = 500) String care,
        @Size(max = 500) String warranty,
        ProductBadge badge,
        @Min(0) int priceCents,
        @Min(0) Integer compareAtPriceCents,
        boolean published,
        @NotNull Long categoryId,
        Long brandId) {
}
