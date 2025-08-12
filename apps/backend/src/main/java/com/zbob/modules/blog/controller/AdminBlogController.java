package com.zbob.modules.blog.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.blog.dto.BlogPostRequest;
import com.zbob.modules.blog.dto.BlogPostResponse;
import com.zbob.modules.blog.dto.BlogPostSummaryResponse;
import com.zbob.modules.blog.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/blog")
public class AdminBlogController {

    private final BlogService blog;

    public AdminBlogController(BlogService blog) {
        this.blog = blog;
    }

    @GetMapping
    PageResponse<BlogPostSummaryResponse> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return blog.listAll(pageable);
    }

    @GetMapping("/{id}")
    BlogPostResponse get(@PathVariable Long id) {
        return blog.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BlogPostResponse create(@Valid @RequestBody BlogPostRequest request) {
        return blog.create(request);
    }

    @PutMapping("/{id}")
    BlogPostResponse update(@PathVariable Long id, @Valid @RequestBody BlogPostRequest request) {
        return blog.update(id, request);
    }

    @PutMapping(path = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BlogPostResponse uploadCover(@PathVariable Long id, @RequestPart MultipartFile file) {
        return blog.uploadCover(id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        blog.delete(id);
    }
}
