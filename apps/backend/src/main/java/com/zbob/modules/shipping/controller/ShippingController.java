package com.zbob.modules.shipping.controller;

import com.zbob.modules.shipping.dto.ShippingMethodResponse;
import com.zbob.modules.shipping.service.ShippingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping-methods")
public class ShippingController {

    private final ShippingService shipping;

    public ShippingController(ShippingService shipping) {
        this.shipping = shipping;
    }

    @GetMapping
    List<ShippingMethodResponse> list() {
        return shipping.listActive();
    }
}
