package com.waytoearth.dto.request.journey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "갤러리 이미지 순서 변경 요청")
public record GalleryImagesReorderRequest(
    @NotEmpty(message = "이미지 ID 목록은 비어 있을 수 없습니다")
    @Schema(description = "정렬 순서대로 나열된 이미지 ID 목록")
    List<Long> imageIds
) {}

