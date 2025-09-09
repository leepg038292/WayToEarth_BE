package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가상 코스 전체 진행률 응답 DTO")
public record VirtualCourseProgressResponse(
        @Schema(description = "진행률 (%)", example = "45.2") Double progressPercent,
        @Schema(description = "총 누적 거리 (km)", example = "123.4") Double totalDistanceAccumulated,
        @Schema(description = "상태", example = "ACTIVE") String status
) {}


