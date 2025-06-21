package com.zbob.modules.products.dto.response;

import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.products.entity.ProductBadge;
import java.util.List;

/** Storefront product page. Reports only whether it is in stock, never how many are left. */
public record PublicProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        String material,
        String care,
        String warranty,
        ProductBadge badge,
        int priceCents,
        Integer compareAtPriceCents,
        boolean inStock,
        CategoryResponse category,
        List<ProductImageResponse> images) {
}
