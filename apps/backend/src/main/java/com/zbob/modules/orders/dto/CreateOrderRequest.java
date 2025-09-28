package com.zbob.modules.orders.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(@NotNull Long addressId, @NotNull Long shippingMethodId, @Size(max = 40) String promotionCode) {
}
