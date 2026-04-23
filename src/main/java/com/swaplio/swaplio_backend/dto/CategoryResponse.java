package com.swaplio.swaplio_backend.dto;

import com.swaplio.swaplio_backend.model.Category;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryResponse(
         UUID id,
         String name,
         String slug
) {
    public static CategoryResponse toResponse(Category c){
        return CategoryResponse.builder()
                .id(c.getId())
                .slug(c.getSlug())
                .name(c.getName())
                .build();
    }
}
