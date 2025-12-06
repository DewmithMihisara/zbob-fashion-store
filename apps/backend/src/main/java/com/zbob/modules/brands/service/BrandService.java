package com.zbob.modules.brands.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.brands.dto.request.BrandRequest;
import com.zbob.modules.brands.dto.response.BrandResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BrandService {

    List<BrandResponse> list();

    PageResponse<BrandResponse> listPaged(Pageable pageable);

    BrandResponse create(BrandRequest request);

    BrandResponse update(Long id, BrandRequest request);

    void delete(Long id);
}
