package com.waytoearth.dto.response.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 날씨 응답 DTO")
public record SegmentWeatherResponse(
        @Schema(description = "세그먼트 ID", example = "100") Long segmentId,
        @Schema(description = "날씨 상태", example = "맑음") String condition,
        @Schema(description = "기온 (℃)", example = "26.5") Double temperature,
        @Schema(description = "풍속 (m/s)", example = "2.3") Double windSpeed
) {}
