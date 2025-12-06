package com.zbob.modules.customers.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.customers.dto.CreateCustomerRequest;
import com.zbob.modules.customers.dto.UpdateCustomerRequest;
import com.zbob.modules.customers.service.CustomerService;
import com.zbob.modules.users.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return customers.createCustomer(request);
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest request) {
        return customers.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable Long id) {
        customers.deleteCustomer(id);
    }

    @PatchMapping("/{id}")
    UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return customers.setCustomerActive(id, active);
    }
}
