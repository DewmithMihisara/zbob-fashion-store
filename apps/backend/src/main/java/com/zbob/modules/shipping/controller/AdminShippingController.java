package com.zbob.modules.shipping.controller;

import com.zbob.modules.shipping.dto.ShippingMethodRequest;
import com.zbob.modules.shipping.dto.ShippingMethodResponse;
import com.zbob.modules.shipping.service.ShippingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/shipping-methods")
public class AdminShippingController {

    private final ShippingService shipping;

    public AdminShippingController(ShippingService shipping) {
        this.shipping = shipping;
    }

    @GetMapping
    List<ShippingMethodResponse> list() {
        return shipping.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ShippingMethodResponse create(@Valid @RequestBody ShippingMethodRequest request) {
        return shipping.create(request);
    }

    @PutMapping("/{id}")
    ShippingMethodResponse update(@PathVariable Long id, @Valid @RequestBody ShippingMethodRequest request) {
        return shipping.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        shipping.delete(id);
    }
}
