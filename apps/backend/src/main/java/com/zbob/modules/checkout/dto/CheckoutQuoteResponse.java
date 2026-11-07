package com.zbob.modules.checkout.dto;

import java.util.List;

/** readyToPay is true only when everything is chosen, everything is in stock and a payment gateway exists. */
public record CheckoutQuoteResponse(
        List<Line> lines,
        int subtotalCents,
        int discountCents,
        int shippingCents,
        int totalCents,
        String promotionCode,
        String promotionError,
        String shippingMethod,
        Long addressId,
        boolean paymentAvailable,
        boolean readyToPay,
        List<String> problems) {

    public record Line(Long variantId, String productName, String productSlug, String size, String color, int quantity,
            int unitPriceCents, int lineTotalCents, boolean available) {
    }
}
