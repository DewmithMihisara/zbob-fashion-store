package com.zbob.modules.inventory.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.inventory.dto.request.BulkStockRequest;
import com.zbob.modules.inventory.dto.request.StockUpdateRequest;
import com.zbob.modules.inventory.dto.request.VariantRequest;
import com.zbob.modules.inventory.dto.response.InventoryRowResponse;
import com.zbob.modules.inventory.dto.response.VariantResponse;
import com.zbob.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/admin")
public class AdminInventoryController {

    private final InventoryService inventory;

    public AdminInventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/inventory")
    PageResponse<InventoryRowResponse> list(
            @RequestParam(defaultValue = "false") boolean lowStockOnly,
            @PageableDefault(size = 50, sort = {"product.name", "size", "color"}) Pageable pageable) {
        return inventory.list(lowStockOnly, pageable);
    }

    @PostMapping("/inventory/bulk")
    Map<String, Integer> bulk(@Valid @RequestBody BulkStockRequest request) {
        return Map.of("updated", inventory.bulkUpdateStock(request));
    }

    @GetMapping("/products/{productId}/variants")
    PageResponse<VariantResponse> listForProduct(@PathVariable Long productId,
            @PageableDefault(size = 10, sort = {"size", "color"}) Pageable pageable) {
        return inventory.listForProduct(productId, pageable);
    }

    @PostMapping("/products/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    VariantResponse add(@PathVariable Long productId, @Valid @RequestBody VariantRequest request) {
        return inventory.addVariant(productId, request);
    }

    @PutMapping("/variants/{id}")
    VariantResponse update(@PathVariable Long id, @Valid @RequestBody VariantRequest request) {
        return inventory.updateVariant(id, request);
    }

    @PatchMapping("/variants/{id}/stock")
    VariantResponse updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return inventory.updateStock(id, request);
    }

    @DeleteMapping("/variants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        inventory.deleteVariant(id);
    }
}
