package com.zbob.modules.customers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 120) String fullName,
        boolean active) {
}
