package com.zbob.modules.contact.repository;

import com.zbob.modules.contact.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    boolean existsByEmail(String email);
}
