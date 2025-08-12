package com.zbob.modules.blog.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.blog.dto.BlogPostResponse;
import com.zbob.modules.blog.dto.BlogPostSummaryResponse;
import com.zbob.modules.blog.service.BlogService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogController {

    private final BlogService blog;

    public BlogController(BlogService blog) {
        this.blog = blog;
    }

    @GetMapping
    PageResponse<BlogPostSummaryResponse> list(
            @RequestParam(required = false) String topic,
            @PageableDefault(size = 12, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return blog.listPublished(topic, pageable);
    }

    @GetMapping("/{slug}")
    BlogPostResponse get(@PathVariable String slug) {
        return blog.getPublished(slug);
    }
}
