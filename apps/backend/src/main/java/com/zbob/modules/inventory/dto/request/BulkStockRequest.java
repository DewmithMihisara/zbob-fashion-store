package com.zbob.modules.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** SET puts every chosen row at the amount; ADJUST adds it, or takes it away when negative. */
public record BulkStockRequest(@NotEmpty @Size(max = 200) List<Long> variantIds, @NotNull Mode mode, int amount) {

    public enum Mode {
        SET,
        ADJUST
    }
}
