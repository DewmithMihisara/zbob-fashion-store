package com.zbob.modules.orders.dto;

import com.zbob.modules.orders.entity.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        int subtotalCents,
        int discountCents,
        int shippingCents,
        int totalCents,
        String promotionCode,
        String shippingMethod,
        String recipient,
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String country,
        String phone,
        Instant createdAt,
        List<OrderItemResponse> items) {
}
