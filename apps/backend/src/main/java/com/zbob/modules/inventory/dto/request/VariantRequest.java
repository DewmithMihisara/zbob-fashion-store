package com.zbob.modules.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VariantRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 20) String size,
        @NotBlank @Size(max = 40) String color,
        @Min(0) int stockQuantity,
        @Min(0) int lowStockThreshold) {
}
