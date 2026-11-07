package com.zbob.modules.products.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReorderImagesRequest(@NotNull List<Long> imageIds) {
}
