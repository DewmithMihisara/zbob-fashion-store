package com.zbob.modules.carts.dto;

public record CartItemResponse(
        Long id, Long variantId, String productName, String productSlug, String imageUrl, String size, String color,
        int quantity, int unitPriceCents, int lineTotalCents, boolean available) {
}
