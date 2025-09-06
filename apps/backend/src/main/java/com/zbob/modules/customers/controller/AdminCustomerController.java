package com.zbob.modules.customers.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.customers.service.CustomerService;
import com.zbob.modules.users.dto.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    private final CustomerService customers;

    public AdminCustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    PageResponse<UserResponse> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return customers.listCustomers(pageable);
    }

    @PatchMapping("/{id}")
    UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return customers.setCustomerActive(id, active);
    }
}
