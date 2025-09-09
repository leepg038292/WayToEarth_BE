package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 상세 응답 DTO")
public record CourseSegmentDetailResponse(
        @Schema(description = "세그먼트 ID", example = "100") Long id,
        @Schema(description = "타입", example = "DOMESTIC") String type,
        @Schema(description = "시작 위도", example = "37.5665") Double startLat,
        @Schema(description = "시작 경도", example = "126.9780") Double startLng,
        @Schema(description = "종료 위도", example = "35.1796") Double endLat,
        @Schema(description = "종료 경도", example = "129.0756") Double endLng,
        @Schema(description = "거리 (km)", example = "45.7") Double distanceKm
) {}