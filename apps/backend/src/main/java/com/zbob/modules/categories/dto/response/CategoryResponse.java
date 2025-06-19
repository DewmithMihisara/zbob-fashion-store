package com.zbob.modules.categories.dto.response;

public record CategoryResponse(Long id, String name, String slug, String description, String badge, int position) {
}
