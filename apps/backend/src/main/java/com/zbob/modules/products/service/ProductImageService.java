package com.zbob.modules.products.service;

import com.zbob.common.util.AfterCommit;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.products.dto.response.ProductImageResponse;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductImage;
import com.zbob.modules.products.mapper.ProductMapper;
import com.zbob.modules.products.repository.ProductImageRepository;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.storage.ImageUploader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageService {

    private final ProductImageRepository images;
    private final ProductRepository products;
    private final ImageUploader uploader;

    public ProductImageService(ProductImageRepository images, ProductRepository products, ImageUploader uploader) {
        this.images = images;
        this.products = products;
        this.uploader = uploader;
    }

    @Transactional
    public ProductImageResponse upload(Long productId, MultipartFile file, String altText) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        ImageUploader.StoredImage stored = uploader.upload("products/" + productId, file);
        int position = images.countByProductId(productId);
        return ProductMapper.toImage(images.save(new ProductImage(product, stored.key(), stored.url(), altText, position)));
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        ProductImage image = images.findById(imageId)
                .filter(i -> i.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Image", imageId));
        images.delete(image);
        String key = image.getStorageKey();
        AfterCommit.run(() -> uploader.deleteQuietly(key));
    }

    /** Schedules removal of all of a product's stored files once its database delete has committed. */
    public void deleteFilesAfterCommit(Long productId) {
        List<String> keys = images.findByProductIdOrderByPositionAscIdAsc(productId).stream()
                .map(ProductImage::getStorageKey)
                .toList();
        AfterCommit.run(() -> keys.forEach(uploader::deleteQuietly));
    }

    /** Sets the display order. The list must contain exactly the product's images. */
    @Transactional
    public List<ProductImageResponse> reorder(Long productId, List<Long> imageIds) {
        List<ProductImage> current = images.findByProductIdOrderByPositionAscIdAsc(productId);
        Map<Long, ProductImage> byId = current.stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));
        if (imageIds.size() != current.size() || !new HashSet<>(imageIds).equals(byId.keySet())) {
            throw new BadRequestException("The new order must list each of this product's images once");
        }
        for (int i = 0; i < imageIds.size(); i++) {
            byId.get(imageIds.get(i)).setPosition(i);
        }
        return imageIds.stream().map(byId::get).map(ProductMapper::toImage).toList();
    }
}
