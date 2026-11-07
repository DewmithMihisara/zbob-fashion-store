package com.zbob.modules.blog.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.common.util.AfterCommit;
import com.zbob.common.util.Slugs;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.blog.dto.BlogPostRequest;
import com.zbob.modules.blog.dto.BlogPostResponse;
import com.zbob.modules.blog.dto.BlogPostSummaryResponse;
import com.zbob.modules.blog.entity.BlogPost;
import com.zbob.modules.blog.repository.BlogPostRepository;
import com.zbob.modules.storage.ImageUploader;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BlogService {

    private final BlogPostRepository posts;
    private final ImageUploader uploader;

    public BlogService(BlogPostRepository posts, ImageUploader uploader) {
        this.posts = posts;
        this.uploader = uploader;
    }

    @Transactional(readOnly = true)
    public PageResponse<BlogPostSummaryResponse> listPublished(String topic, Pageable pageable) {
        var page = StringUtils.hasText(topic)
                ? posts.findByPublishedAtNotNullAndTopic(topic, pageable)
                : posts.findByPublishedAtNotNull(pageable);
        return PageResponse.of(page, BlogService::toSummary);
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getPublished(String slug) {
        return toResponse(posts.findBySlugAndPublishedAtNotNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post", slug)));
    }

    @Transactional(readOnly = true)
    public PageResponse<BlogPostSummaryResponse> listAll(Pageable pageable) {
        return PageResponse.of(posts.findAll(pageable), BlogService::toSummary);
    }

    @Transactional(readOnly = true)
    public BlogPostResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public BlogPostResponse create(BlogPostRequest request) {
        String slug = Slugs.of(request.title());
        if (posts.existsBySlug(slug)) {
            throw new ConflictException("A post with this title already exists");
        }
        BlogPost post = new BlogPost(request.title().trim(), slug);
        apply(post, request);
        return toResponse(posts.save(post));
    }

    @Transactional
    public BlogPostResponse update(Long id, BlogPostRequest request) {
        BlogPost post = find(id);
        post.setTitle(request.title().trim());
        apply(post, request);
        return toResponse(post);
    }

    @Transactional
    public BlogPostResponse uploadCover(Long id, MultipartFile file) {
        BlogPost post = find(id);
        ImageUploader.StoredImage stored = uploader.upload("blog/" + id, file);
        String oldKey = post.getCoverStorageKey();
        post.setCover(stored.key(), stored.url());
        if (oldKey != null) {
            AfterCommit.run(() -> uploader.deleteQuietly(oldKey));
        }
        return toResponse(post);
    }

    @Transactional
    public void delete(Long id) {
        BlogPost post = find(id);
        posts.delete(post);
        String key = post.getCoverStorageKey();
        if (key != null) {
            AfterCommit.run(() -> uploader.deleteQuietly(key));
        }
    }

    private BlogPost find(Long id) {
        return posts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }

    /** The first publish sets the date; later edits keep it, and unpublishing clears it. */
    private static void apply(BlogPost post, BlogPostRequest request) {
        post.setExcerpt(request.excerpt());
        post.setBody(request.body());
        post.setTopic(request.topic());
        post.setReadMinutes(request.readMinutes());
        if (!request.published()) {
            post.setPublishedAt(null);
        } else if (post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
    }

    private static BlogPostSummaryResponse toSummary(BlogPost p) {
        return new BlogPostSummaryResponse(p.getId(), p.getTitle(), p.getSlug(), p.getExcerpt(), p.getTopic(),
                p.getReadMinutes(), p.getCoverUrl(), p.getPublishedAt());
    }

    private static BlogPostResponse toResponse(BlogPost p) {
        return new BlogPostResponse(p.getId(), p.getTitle(), p.getSlug(), p.getExcerpt(), p.getBody(), p.getTopic(),
                p.getReadMinutes(), p.getCoverUrl(), p.getPublishedAt());
    }
}
