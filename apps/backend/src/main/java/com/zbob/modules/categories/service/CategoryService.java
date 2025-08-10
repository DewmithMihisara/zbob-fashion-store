package com.zbob.modules.categories.service;

import com.zbob.modules.categories.dto.request.CategoryRequest;
import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.categories.dto.response.CategoryWithPricesResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryResponse> list();

    List<CategoryWithPricesResponse> listWithPrices();

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
