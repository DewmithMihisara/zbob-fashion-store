package com.zbob.modules.blog.repository;

import com.zbob.modules.blog.entity.BlogPost;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Page<BlogPost> findByPublishedAtNotNull(Pageable pageable);

    Page<BlogPost> findByPublishedAtNotNullAndTopic(String topic, Pageable pageable);

    Optional<BlogPost> findBySlugAndPublishedAtNotNull(String slug);

    boolean existsBySlug(String slug);
}
