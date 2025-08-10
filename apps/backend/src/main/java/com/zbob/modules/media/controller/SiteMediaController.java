package com.zbob.modules.media.controller;

import com.zbob.modules.media.service.SiteMediaService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/site-media")
public class SiteMediaController {

    private final SiteMediaService media;

    public SiteMediaController(SiteMediaService media) {
        this.media = media;
    }

    @GetMapping
    Map<String, String> urls() {
        return media.urls();
    }
}
