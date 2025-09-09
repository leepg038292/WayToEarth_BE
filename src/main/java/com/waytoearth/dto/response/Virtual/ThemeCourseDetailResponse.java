package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "테마 코스 상세 응답 DTO")
public record ThemeCourseDetailResponse(
        @Schema(description = "코스 ID", example = "1") Long id,
        @Schema(description = "코스 제목", example = "서울 → 부산") String title,
        @Schema(description = "코스 설명", example = "국내 대표 장거리 러닝 코스") String description,
        @Schema(description = "총 거리 (km)", example = "350.5") Double totalDistanceKm,
        @Schema(description = "세그먼트 목록") List<CourseSegmentDetailResponse> segments
) {}
