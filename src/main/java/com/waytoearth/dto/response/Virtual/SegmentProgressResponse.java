package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 진행률 응답 DTO")
public record SegmentProgressResponse(
        @Schema(description = "세그먼트 ID", example = "100") Long segmentId,
        @Schema(description = "세그먼트 누적 거리 (km)", example = "15.2") Double distanceAccumulated,
        @Schema(description = "상태", example = "ACTIVE") String status
) {}