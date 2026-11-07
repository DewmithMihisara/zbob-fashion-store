package com.zbob.modules.products.dto.response;

import com.zbob.modules.brands.dto.response.BrandResponse;
import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.inventory.dto.response.VariantResponse;
import com.zbob.modules.products.entity.ProductBadge;
import java.util.List;

/** Full shape for the admin editor. The storefront receives PublicProductResponse instead. */
public record ProductResponse(
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
        boolean published,
        boolean inStock,
        CategoryResponse category,
        BrandResponse brand,
        List<ProductImageResponse> images,
        List<VariantResponse> variants) {
}
