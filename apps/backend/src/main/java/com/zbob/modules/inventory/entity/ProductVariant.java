package com.zbob.modules.inventory.entity;

import com.zbob.common.entity.BaseEntity;
import com.zbob.modules.products.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One sellable size and colour of a product, with its own stock count. */
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 20)
    private String size;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 5;

    /** Shown as the swatch colour on the storefront, e.g. #1F2A44. Optional. */
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    protected ProductVariant() {
    }

    public ProductVariant(Product product, String sku, String size, String color, int stockQuantity) {
        this.product = product;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold;
    }
}
