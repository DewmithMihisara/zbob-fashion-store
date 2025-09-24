package com.zbob.modules.carts.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotNull Long variantId, @Min(1) @Max(99) int quantity) {
}
