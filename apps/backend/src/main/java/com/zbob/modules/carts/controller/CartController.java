package com.zbob.modules.carts.controller;

import com.zbob.modules.carts.dto.AddCartItemRequest;
import com.zbob.modules.carts.dto.CartResponse;
import com.zbob.modules.carts.dto.UpdateCartItemRequest;
import com.zbob.modules.carts.service.CartService;
import com.zbob.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @GetMapping
    CartResponse get(@AuthenticationPrincipal AuthenticatedUser user) {
        return carts.get(user.id());
    }

    @PostMapping("/items")
    CartResponse add(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AddCartItemRequest request) {
        return carts.add(user.id(), request);
    }

    @PatchMapping("/items/{id}")
    CartResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return carts.updateQuantity(user.id(), id, request.quantity());
    }

    @DeleteMapping("/items/{id}")
    CartResponse remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return carts.remove(user.id(), id);
    }
}
