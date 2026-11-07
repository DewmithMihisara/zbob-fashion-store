package com.zbob.modules.media.repository;

import com.zbob.modules.media.entity.SiteMedia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteMediaRepository extends JpaRepository<SiteMedia, Long> {

    Optional<SiteMedia> findBySlot(String slot);
}
