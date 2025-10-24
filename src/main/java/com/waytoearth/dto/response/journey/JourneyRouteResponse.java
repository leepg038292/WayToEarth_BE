package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.journey.JourneyRouteEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "여정 경로 응답")
public record JourneyRouteResponse(
    @Schema(description = "경로 ID", example = "1")
    Long id,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "경로 순서", example = "1")
    Integer sequence,

    @Schema(description = "고도 (미터)", example = "120.5")
    Double altitude,

    @Schema(description = "구간 설명", example = "한강대교 진입")
    String description
) {
    public static JourneyRouteResponse from(JourneyRouteEntity route) {
        return new JourneyRouteResponse(
            route.getId(),
            route.getLatitude(),
            route.getLongitude(),
            route.getSequence(),
            route.getAltitude(),
            route.getDescription()
        );
    }
}