package com.zbob.modules.wishlist.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.wishlist.service.WishlistService;
import com.zbob.security.AuthenticatedUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/wishlist")
@PreAuthorize("hasRole('CUSTOMER')")
public class WishlistController {

    private final WishlistService wishlist;

    public WishlistController(WishlistService wishlist) {
        this.wishlist = wishlist;
    }

    @GetMapping
    PageResponse<ProductSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return wishlist.list(user.id(), pageable);
    }

    @GetMapping("/ids")
    List<Long> ids(@AuthenticationPrincipal AuthenticatedUser user) {
        return wishlist.productIds(user.id());
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void add(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
        wishlist.add(user.id(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
        wishlist.remove(user.id(), productId);
    }
}
