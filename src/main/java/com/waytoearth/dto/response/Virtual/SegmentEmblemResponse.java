package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 엠블럼 응답 DTO")
public record SegmentEmblemResponse(
        @Schema(description = "세그먼트 ID", example = "100") Long segmentId,
        @Schema(description = "엠블럼 이름", example = "10km 달성!") String name,
        @Schema(description = "엠블럼 코드", example = "EMBLEM_10K") String code
) {}
