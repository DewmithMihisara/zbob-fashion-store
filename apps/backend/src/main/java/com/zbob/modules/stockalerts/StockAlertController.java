package com.zbob.modules.stockalerts;

import com.zbob.common.ratelimit.RateLimits;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockAlertController {

    public record StockAlertRequest(@NotNull Long variantId, @NotBlank @Email @Size(max = 255) String email) {
    }

    private final StockAlertService alerts;
    private final RateLimits limits;

    public StockAlertController(StockAlertService alerts, RateLimits limits) {
        this.alerts = alerts;
        this.limits = limits;
    }

    @PostMapping("/api/v1/stock-alerts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void subscribe(@Valid @RequestBody StockAlertRequest request, HttpServletRequest http) {
        RateLimits.enforce(limits.formsPerIp().tryAcquire("forms:" + http.getRemoteAddr()));
        alerts.subscribe(request.variantId(), request.email());
    }
}
