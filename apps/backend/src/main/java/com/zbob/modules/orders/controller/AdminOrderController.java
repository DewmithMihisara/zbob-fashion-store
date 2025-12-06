package com.zbob.modules.orders.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.orders.dto.OrderItemResponse;
import com.zbob.modules.orders.dto.OrderResponse;
import com.zbob.modules.orders.dto.OrderSummaryResponse;
import com.zbob.modules.orders.dto.UpdateOrderStatusRequest;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orders;

    public AdminOrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    PageResponse<OrderSummaryResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orders.listAll(status, pageable);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) {
        return orders.get(id);
    }

    @GetMapping("/{id}/items")
    PageResponse<OrderItemResponse> items(@PathVariable Long id, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return orders.listItems(id, pageable);
    }

    @PatchMapping("/{id}/status")
    OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orders.updateStatus(id, request.status());
    }
}
