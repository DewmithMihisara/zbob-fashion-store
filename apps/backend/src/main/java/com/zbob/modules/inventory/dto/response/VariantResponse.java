package com.zbob.modules.inventory.dto.response;

public record VariantResponse(
        Long id, String sku, String size, String color, int stockQuantity, int lowStockThreshold, boolean lowStock,
        String colorHex) {
}
