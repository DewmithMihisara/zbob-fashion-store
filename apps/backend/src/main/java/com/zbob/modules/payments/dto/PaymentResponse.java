package com.zbob.modules.payments.dto;

import com.zbob.modules.payments.entity.PaymentStatus;

public record PaymentResponse(Long id, Long orderId, String provider, PaymentStatus status, int amountCents, String reference) {
}
