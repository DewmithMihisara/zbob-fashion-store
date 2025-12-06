package com.zbob.modules.categories.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.categories.dto.request.CategoryRequest;
import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.categories.service.CategoryService;
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
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final CategoryService categories;

    public AdminCategoryController(CategoryService categories) {
        this.categories = categories;
    }

    @GetMapping
    PageResponse<CategoryResponse> list(@PageableDefault(size = 20, sort = {"position", "name"}) Pageable pageable) {
        return categories.listPaged(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categories.create(request);
    }

    @PutMapping("/{id}")
    CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categories.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable Long id) {
        categories.delete(id);
    }
}
