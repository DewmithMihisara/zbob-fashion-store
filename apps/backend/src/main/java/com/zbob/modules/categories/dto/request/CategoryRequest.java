package com.zbob.modules.categories.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 500) String description,
        @Size(max = 20) String badge,
        int position,
        @Size(max = 10000) String sizeGuide) {
}
