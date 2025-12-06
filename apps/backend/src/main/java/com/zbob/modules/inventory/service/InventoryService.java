package com.zbob.modules.inventory.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.inventory.dto.request.StockUpdateRequest;
import com.zbob.modules.inventory.dto.request.VariantRequest;
import com.zbob.modules.inventory.dto.response.InventoryRowResponse;
import com.zbob.modules.inventory.dto.response.VariantResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    PageResponse<InventoryRowResponse> list(boolean lowStockOnly, Pageable pageable);

    PageResponse<VariantResponse> listForProduct(Long productId, Pageable pageable);

    VariantResponse addVariant(Long productId, VariantRequest request);

    VariantResponse updateVariant(Long variantId, VariantRequest request);

    VariantResponse updateStock(Long variantId, StockUpdateRequest request);

    void deleteVariant(Long variantId);
}
