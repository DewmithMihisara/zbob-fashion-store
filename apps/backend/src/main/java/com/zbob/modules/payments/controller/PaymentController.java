package com.zbob.modules.payments.controller;

import com.zbob.modules.payments.dto.PaymentResponse;
import com.zbob.modules.payments.service.PaymentService;
import com.zbob.security.AuthenticatedUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/api/v1/orders/{orderId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    PaymentResponse start(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId) {
        return payments.start(user.id(), orderId);
    }

    @GetMapping("/api/v1/admin/orders/{orderId}/payments")
    List<PaymentResponse> forOrder(@PathVariable Long orderId) {
        return payments.forOrder(orderId);
    }
}
