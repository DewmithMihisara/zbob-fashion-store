package com.zbob.modules.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Lets staff log an enquiry that came in another way, such as by phone, or correct one. */
public record AdminMessageRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 200) String subject,
        @NotBlank @Size(max = 4000) String message,
        boolean handled) {
}
