package com.zbob.modules.brands.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.brands.dto.request.BrandRequest;
import com.zbob.modules.brands.dto.response.BrandResponse;
import com.zbob.modules.brands.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/brands")
public class AdminBrandController {

    private final BrandService brands;

    public AdminBrandController(BrandService brands) {
        this.brands = brands;
    }

    @GetMapping
    PageResponse<BrandResponse> list(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return brands.listPaged(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BrandResponse create(@Valid @RequestBody BrandRequest request) {
        return brands.create(request);
    }

    @PutMapping("/{id}")
    BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return brands.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable Long id) {
        brands.delete(id);
    }
}
