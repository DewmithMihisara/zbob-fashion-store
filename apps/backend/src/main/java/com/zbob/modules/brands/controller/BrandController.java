package com.zbob.modules.brands.controller;

import com.zbob.modules.brands.dto.response.BrandResponse;
import com.zbob.modules.brands.service.BrandService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brands;

    public BrandController(BrandService brands) {
        this.brands = brands;
    }

    @GetMapping
    List<BrandResponse> list() {
        return brands.list();
    }
}
