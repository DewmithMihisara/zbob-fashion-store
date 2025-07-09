package com.zbob.modules.products.controller;

import com.zbob.modules.products.dto.request.ReorderImagesRequest;
import com.zbob.modules.products.dto.response.ProductImageResponse;
import com.zbob.modules.products.service.ProductImageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
public class AdminProductImageController {

    private final ProductImageService images;

    public AdminProductImageController(ProductImageService images) {
        this.images = images;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ProductImageResponse upload(@PathVariable Long productId, @RequestPart MultipartFile file,
            @RequestParam(required = false) String altText) {
        return images.upload(productId, file, altText);
    }

    @PutMapping("/order")
    List<ProductImageResponse> reorder(@PathVariable Long productId, @Valid @RequestBody ReorderImagesRequest request) {
        return images.reorder(productId, request.imageIds());
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long productId, @PathVariable Long imageId) {
        images.delete(productId, imageId);
    }
}
