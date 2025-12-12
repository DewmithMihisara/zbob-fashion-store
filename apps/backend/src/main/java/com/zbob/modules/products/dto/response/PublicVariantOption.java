package com.zbob.modules.products.dto.response;

/** One size and colour shoppers can choose. Says only whether it is available, never how many are left. */
public record PublicVariantOption(Long variantId, String size, String color, boolean inStock) {
}
