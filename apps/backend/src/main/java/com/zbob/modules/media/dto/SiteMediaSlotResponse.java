package com.zbob.modules.media.dto;

/** A slot as shown in the admin dashboard; url is null while the placeholder is showing. */
public record SiteMediaSlotResponse(String slot, String label, String url) {
}
