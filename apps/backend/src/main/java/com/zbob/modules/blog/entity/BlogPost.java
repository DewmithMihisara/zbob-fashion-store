package com.zbob.modules.blog.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "blog_posts")
public class BlogPost extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 40)
    private String topic;

    @Column(name = "read_minutes", nullable = false)
    private int readMinutes = 5;

    @Column(name = "cover_storage_key")
    private String coverStorageKey;

    @Column(name = "cover_url", length = 1000)
    private String coverUrl;

    /** Null while the post is a draft. */
    @Column(name = "published_at")
    private Instant publishedAt;

    protected BlogPost() {
    }

    public BlogPost(String title, String slug) {
        this.title = title;
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getReadMinutes() {
        return readMinutes;
    }

    public void setReadMinutes(int readMinutes) {
        this.readMinutes = readMinutes;
    }

    public String getCoverStorageKey() {
        return coverStorageKey;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCover(String storageKey, String url) {
        this.coverStorageKey = storageKey;
        this.coverUrl = url;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
