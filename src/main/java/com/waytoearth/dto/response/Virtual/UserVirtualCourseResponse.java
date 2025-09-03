package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 가상 코스 등록 응답 DTO")
public record UserVirtualCourseResponse(

        @Schema(description = "등록된 UserVirtualCourse ID", example = "100")
        Long id,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "코스 ID", example = "10")
        Long courseId,

        @Schema(description = "코스 타입", example = "CUSTOM")
        String courseType,

        @Schema(description = "누적 거리 (km)", example = "0.0")
        Double totalDistanceAccumulated,

        @Schema(description = "진행률 (%)", example = "0.0")
        Double progressPercent,

        @Schema(description = "코스 진행 상태", example = "ONGOING")
        String status
) {}
