package com.zbob.modules.brands.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "brands")
public class Brand extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    protected Brand() {
    }

    public Brand(String name, String slug) {
        this.name = name;
        this.slug = slug;
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
}
