package com.zbob.modules.products.dto.response;

import com.zbob.modules.products.entity.ProductBadge;

/** Card shape; matches ProductSummary in @zbob/shared-types. */
public record ProductSummaryResponse(
        Long id,
        String name,
        String slug,
        ProductBadge badge,
        int priceCents,
        Integer compareAtPriceCents,
        String imageUrl,
        String categorySlug,
        boolean inStock,
        boolean published) {
}
