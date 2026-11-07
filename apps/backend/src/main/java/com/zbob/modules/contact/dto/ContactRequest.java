package com.zbob.modules.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The website field is hidden in the form; only bots fill it in. */
public record ContactRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 200) String subject,
        @NotBlank @Size(max = 4000) String message,
        String website) {
}
