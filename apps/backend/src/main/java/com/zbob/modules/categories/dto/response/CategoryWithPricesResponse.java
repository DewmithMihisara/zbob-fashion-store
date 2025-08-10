package com.zbob.modules.categories.dto.response;

/** Category card on the home page; the prices are null when it has no published products. */
public record CategoryWithPricesResponse(
        Long id, String name, String slug, String description, String badge, Integer minPriceCents, Integer maxPriceCents) {
}
