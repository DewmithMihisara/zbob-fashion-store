package com.zbob.modules.checkout;

import com.zbob.modules.checkout.dto.CheckoutQuoteRequest;
import com.zbob.modules.checkout.dto.CheckoutQuoteResponse;
import com.zbob.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final CheckoutService checkout;

    public CheckoutController(CheckoutService checkout) {
        this.checkout = checkout;
    }

    @PostMapping("/api/v1/checkout/quote")
    CheckoutQuoteResponse quote(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CheckoutQuoteRequest request) {
        return checkout.quote(user.id(), request);
    }
}
