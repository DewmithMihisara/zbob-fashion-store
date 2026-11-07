package com.zbob.modules.orders.dto;

public record OrderItemResponse(
        Long id, String productName, String sku, String size, String color, int unitPriceCents, int quantity) {
}
