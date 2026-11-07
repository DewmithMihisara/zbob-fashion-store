package com.zbob.modules.products.entity;

import com.zbob.common.entity.BaseEntity;
import com.zbob.modules.brands.entity.Brand;
import com.zbob.modules.categories.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(length = 500)
    private String material;

    @Column(length = 500)
    private String care;

    @Column(length = 500)
    private String warranty;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductBadge badge;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "compare_at_price_cents")
    private Integer compareAtPriceCents;

    @Column(nullable = false)
    private boolean published;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    protected Product() {
    }

    public Product(String name, String slug, Category category) {
        this.name = name;
        this.slug = slug;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getCare() {
        return care;
    }

    public void setCare(String care) {
        this.care = care;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public ProductBadge getBadge() {
        return badge;
    }

    public void setBadge(ProductBadge badge) {
        this.badge = badge;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(int priceCents) {
        this.priceCents = priceCents;
    }

    public Integer getCompareAtPriceCents() {
        return compareAtPriceCents;
    }

    public void setCompareAtPriceCents(Integer compareAtPriceCents) {
        this.compareAtPriceCents = compareAtPriceCents;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
