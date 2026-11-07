package com.zbob.modules.orders.dto;

import com.zbob.modules.customers.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** An order entered by staff, for example one taken by phone. Prices come from the products, never the request. */
public record AdminCreateOrderRequest(
        @NotNull Long customerId,
        @NotEmpty @Valid List<Line> items,
        @NotNull Long shippingMethodId,
        @Size(max = 40) String promotionCode,
        @NotNull @Valid AddressRequest address) {

    public record Line(@NotNull Long variantId, @Min(1) @Max(99) int quantity) {
    }
}
