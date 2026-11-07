package com.zbob.modules.orders.dto;

import com.zbob.modules.orders.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
