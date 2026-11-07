package com.zbob.modules.shipping.dto;

public record ShippingMethodResponse(
        Long id, String name, String description, int priceCents, int estimatedDays, boolean active) {
}
