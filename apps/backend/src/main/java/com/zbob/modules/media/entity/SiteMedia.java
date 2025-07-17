package com.zbob.modules.media.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "site_media")
public class SiteMedia extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String slot;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(nullable = false, length = 1000)
    private String url;

    protected SiteMedia() {
    }

    public SiteMedia(String slot, String storageKey, String url) {
        this.slot = slot;
        this.storageKey = storageKey;
        this.url = url;
    }

    public String getSlot() {
        return slot;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getUrl() {
        return url;
    }

    public void replace(String storageKey, String url) {
        this.storageKey = storageKey;
        this.url = url;
    }
}
