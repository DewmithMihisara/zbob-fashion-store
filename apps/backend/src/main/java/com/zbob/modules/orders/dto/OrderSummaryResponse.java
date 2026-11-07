package com.zbob.modules.orders.dto;

import com.zbob.modules.orders.entity.OrderStatus;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id, String orderNumber, OrderStatus status, int totalCents, String recipient, Instant createdAt) {
}
