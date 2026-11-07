package com.zbob.modules.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Prices what the customer would pay. With buyNow it prices just that item, as Order Now does;
 * without it, it prices the cart. Nothing is saved and no stock is reserved.
 */
public record CheckoutQuoteRequest(Long addressId, Long shippingMethodId, @Size(max = 40) String promotionCode, @Valid BuyNow buyNow) {

    public record BuyNow(@NotNull Long variantId, @Min(1) @Max(99) int quantity) {
    }
}
