package com.zbob.modules.media.controller;

import com.zbob.modules.media.dto.SiteMediaSlotResponse;
import com.zbob.modules.media.service.SiteMediaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/site-media")
public class AdminSiteMediaController {

    private final SiteMediaService media;

    public AdminSiteMediaController(SiteMediaService media) {
        this.media = media;
    }

    @GetMapping
    List<SiteMediaSlotResponse> slots() {
        return media.slots();
    }

    @PutMapping(path = "/{slot}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SiteMediaSlotResponse upload(@PathVariable String slot, @RequestPart MultipartFile file) {
        return media.upload(slot, file);
    }

    @DeleteMapping("/{slot}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void clear(@PathVariable String slot) {
        media.clear(slot);
    }
}
