package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 요약 응답 DTO")
public record CourseSegmentSummaryResponse(
        @Schema(description = "세그먼트 ID", example = "100") Long id,
        @Schema(description = "순서", example = "1") Integer orderIndex,
        @Schema(description = "거리 (km)", example = "45.7") Double distanceKm
) {}