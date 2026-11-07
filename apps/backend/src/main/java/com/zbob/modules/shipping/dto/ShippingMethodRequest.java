package com.zbob.modules.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingMethodRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 200) String description,
        @Min(0) int priceCents,
        @Min(0) int estimatedDays,
        boolean active) {
}
