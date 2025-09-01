package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테마 코스 목록 응답 DTO")
public record ThemeCourseSummaryResponse(
        @Schema(description = "코스 ID", example = "1") Long id,
        @Schema(description = "코스 제목", example = "서울 → 부산") String title,
        @Schema(description = "총 거리 (km)", example = "350.5") Double totalDistanceKm
) {}