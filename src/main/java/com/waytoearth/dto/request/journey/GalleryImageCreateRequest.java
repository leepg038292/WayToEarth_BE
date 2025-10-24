package com.waytoearth.dto.request.journey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "갤러리 이미지 추가 요청")
public record GalleryImageCreateRequest(
    @NotBlank(message = "이미지 URL은 필수입니다")
    @Schema(description = "이미지 URL", example = "https://cdn.waytoearth.com/journeys/1/landmarks/5/uuid.jpg")
    String imageUrl
) {}

