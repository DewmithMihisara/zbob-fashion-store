package com.zbob.modules.inventory.dto.request;

import jakarta.validation.constraints.Min;

public record StockUpdateRequest(@Min(0) int stockQuantity) {
}
