package com.zbob.modules.media.service;

import com.zbob.common.util.AfterCommit;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.media.SiteMediaSlots;
import com.zbob.modules.media.dto.SiteMediaSlotResponse;
import com.zbob.modules.media.entity.SiteMedia;
import com.zbob.modules.media.repository.SiteMediaRepository;
import com.zbob.modules.storage.ImageUploader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SiteMediaService {

    private final SiteMediaRepository media;
    private final ImageUploader uploader;

    public SiteMediaService(SiteMediaRepository media, ImageUploader uploader) {
        this.media = media;
        this.uploader = uploader;
    }

    /** Slot to URL, for the storefront. Slots with no photo are absent. */
    @Transactional(readOnly = true)
    public Map<String, String> urls() {
        return media.findAll().stream().collect(Collectors.toMap(SiteMedia::getSlot, SiteMedia::getUrl));
    }

    @Transactional(readOnly = true)
    public List<SiteMediaSlotResponse> slots() {
        Map<String, String> urls = urls();
        return SiteMediaSlots.ALL.entrySet().stream()
                .map(e -> new SiteMediaSlotResponse(e.getKey(), e.getValue(), urls.get(e.getKey())))
                .toList();
    }

    @Transactional
    public SiteMediaSlotResponse upload(String slot, MultipartFile file) {
        String label = labelOf(slot);
        ImageUploader.StoredImage stored = uploader.upload("site/" + slot, file);
        media.findBySlot(slot).ifPresentOrElse(existing -> {
            String oldKey = existing.getStorageKey();
            existing.replace(stored.key(), stored.url());
            AfterCommit.run(() -> uploader.deleteQuietly(oldKey));
        }, () -> media.save(new SiteMedia(slot, stored.key(), stored.url())));
        return new SiteMediaSlotResponse(slot, label, stored.url());
    }

    @Transactional
    public void clear(String slot) {
        labelOf(slot);
        media.findBySlot(slot).ifPresent(existing -> {
            media.delete(existing);
            String key = existing.getStorageKey();
            AfterCommit.run(() -> uploader.deleteQuietly(key));
        });
    }

    private static String labelOf(String slot) {
        String label = SiteMediaSlots.ALL.get(slot);
        if (label == null) {
            throw new ResourceNotFoundException("Image slot", slot);
        }
        return label;
    }
}
