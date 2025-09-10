package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 랜드마크 응답 DTO")
public record SegmentLandmarkResponse(
        @Schema(description = "랜드마크 ID", example = "501") Long id,
        @Schema(description = "이름", example = "남산타워") String name,
        @Schema(description = "위도", example = "37.5512") Double latitude,
        @Schema(description = "경도", example = "126.9882") Double longitude,
        @Schema(description = "사진 URL") String photoUrl,
        @Schema(description = "설명", example = "서울의 대표적인 전망 명소") String description
) {}
