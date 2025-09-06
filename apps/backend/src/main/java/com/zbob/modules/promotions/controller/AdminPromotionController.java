package com.zbob.modules.promotions.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.promotions.dto.PromotionRequest;
import com.zbob.modules.promotions.dto.PromotionResponse;
import com.zbob.modules.promotions.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/admin/promotions")
public class AdminPromotionController {

    private final PromotionService promotions;

    public AdminPromotionController(PromotionService promotions) {
        this.promotions = promotions;
    }

    @GetMapping
    PageResponse<PromotionResponse> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return promotions.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PromotionResponse create(@Valid @RequestBody PromotionRequest request) {
        return promotions.create(request);
    }

    @PutMapping("/{id}")
    PromotionResponse update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        return promotions.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        promotions.delete(id);
    }
}
