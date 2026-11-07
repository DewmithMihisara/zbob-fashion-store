package com.zbob.modules.carts.dto;

import java.util.List;

public record CartResponse(List<CartItemResponse> items, int subtotalCents) {
}
