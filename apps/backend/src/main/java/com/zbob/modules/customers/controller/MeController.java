package com.zbob.modules.customers.controller;

import com.zbob.modules.customers.dto.AddressRequest;
import com.zbob.modules.customers.dto.AddressResponse;
import com.zbob.modules.customers.dto.ChangePasswordRequest;
import com.zbob.modules.customers.dto.ProfileRequest;
import com.zbob.modules.customers.service.CustomerService;
import com.zbob.modules.users.dto.UserResponse;
import com.zbob.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/me")
@PreAuthorize("hasRole('CUSTOMER')")
public class MeController {

    private final CustomerService customers;

    public MeController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    UserResponse profile(@AuthenticationPrincipal AuthenticatedUser user) {
        return customers.profile(user.id());
    }

    @PutMapping
    UserResponse updateProfile(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ProfileRequest request) {
        return customers.updateProfile(user.id(), request.fullName());
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ChangePasswordRequest request) {
        customers.changePassword(user.id(), request);
    }

    @GetMapping("/addresses")
    List<AddressResponse> addresses(@AuthenticationPrincipal AuthenticatedUser user) {
        return customers.addresses(user.id());
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    AddressResponse addAddress(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AddressRequest request) {
        return customers.addAddress(user.id(), request);
    }

    @PutMapping("/addresses/{id}")
    AddressResponse updateAddress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return customers.updateAddress(user.id(), id, request);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAddress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        customers.deleteAddress(user.id(), id);
    }
}
