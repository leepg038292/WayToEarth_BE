package com.waytoearth.dto.request.journey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "랜드마크 이미지 업데이트 요청")
public record LandmarkImageUpdateRequest(
    @NotBlank(message = "이미지 URL은 필수입니다")
    @Schema(description = "랜드마크 이미지 URL", example = "https://cdn.waytoearth.com/landmarks/eiffel-tower.jpg")
    String imageUrl
) {}
