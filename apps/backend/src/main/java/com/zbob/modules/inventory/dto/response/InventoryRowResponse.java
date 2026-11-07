package com.zbob.modules.inventory.dto.response;

/** One row of the admin stock table. */
public record InventoryRowResponse(
        Long variantId,
        Long productId,
        String productName,
        String sku,
        String size,
        String color,
        int stockQuantity,
        int lowStockThreshold,
        boolean lowStock,
        String colorHex) {
}
