package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.journey.LandmarkEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "랜드마크 요약 응답")
public record LandmarkSummaryResponse(
    @Schema(description = "랜드마크 ID", example = "1")
    Long id,

    @Schema(description = "랜드마크 이름", example = "경복궁")
    String name,

    @Schema(description = "위도", example = "37.5796")
    Double latitude,

    @Schema(description = "경도", example = "126.9770")
    Double longitude,

    @Schema(description = "시작점으로부터 거리 (km)", example = "25.5")
    Double distanceFromStart,

    @Schema(description = "랜드마크 이미지 URL", example = "https://example.com/landmark.jpg")
    String imageUrl,

    @Schema(description = "국가 코드", example = "KR")
    String countryCode,

    @Schema(description = "도시명", example = "서울")
    String cityName
) {
    public static LandmarkSummaryResponse from(LandmarkEntity landmark) {
        return new LandmarkSummaryResponse(
            landmark.getId(),
            landmark.getName(),
            landmark.getLatitude(),
            landmark.getLongitude(),
            landmark.getDistanceFromStart(),
            landmark.getImageUrl(),
            landmark.getCountryCode(),
            landmark.getCityName()
        );
    }
}