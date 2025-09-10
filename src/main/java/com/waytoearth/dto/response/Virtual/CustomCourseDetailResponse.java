package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 커스텀 코스 상세 응답 DTO")
public record CustomCourseDetailResponse(
        @Schema(description = "코스 ID", example = "10") Long id,
        @Schema(description = "제목", example = "한강 러닝 코스") String title,
        @Schema(description = "총 거리 (km)", example = "12.3") Double totalDistanceKm,
        @Schema(description = "세그먼트 목록") List<CourseSegmentDetailResponse> segments
) {}