package com.zbob.modules.brands.service;

import com.zbob.modules.brands.dto.request.BrandRequest;
import com.zbob.modules.brands.dto.response.BrandResponse;
import java.util.List;

public interface BrandService {

    List<BrandResponse> list();

    BrandResponse create(BrandRequest request);

    BrandResponse update(Long id, BrandRequest request);

    void delete(Long id);
}
