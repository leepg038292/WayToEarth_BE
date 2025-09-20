package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.Journey.LandmarkEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "랜드마크 상세 응답")
public record LandmarkDetailResponse(
    @Schema(description = "랜드마크 ID", example = "1")
    Long id,

    @Schema(description = "랜드마크 이름", example = "경복궁")
    String name,

    @Schema(description = "설명", example = "조선왕조의 법궁으로 600년 역사를 자랑하는 궁궐")
    String description,

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
    String cityName,

    @Schema(description = "스토리 카드 목록")
    List<StoryCardResponse> storyCards,

    @Schema(description = "스탬프 수집 여부", example = "true")
    Boolean hasStamp
) {
    public static LandmarkDetailResponse from(
            LandmarkEntity landmark,
            List<StoryCardResponse> storyCards,
            Boolean hasStamp
    ) {
        return new LandmarkDetailResponse(
            landmark.getId(),
            landmark.getName(),
            landmark.getDescription(),
            landmark.getLatitude(),
            landmark.getLongitude(),
            landmark.getDistanceFromStart(),
            landmark.getImageUrl(),
            landmark.getCountryCode(),
            landmark.getCityName(),
            storyCards,
            hasStamp
        );
    }
}