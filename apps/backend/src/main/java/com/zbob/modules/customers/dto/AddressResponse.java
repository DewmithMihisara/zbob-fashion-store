package com.zbob.modules.customers.dto;

public record AddressResponse(
        Long id, String recipient, String line1, String line2, String city, String region, String postalCode,
        String country, String phone) {
}
