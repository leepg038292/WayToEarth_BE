package com.waytoearth.dto.Journey.response;

import com.waytoearth.entity.Journey.JourneyEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "여행 요약 응답")
public record JourneySummaryResponse(
    @Schema(description = "여행 ID", example = "1")
    Long id,

    @Schema(description = "여행 제목", example = "서울에서 부산까지")
    String title,

    @Schema(description = "여행 설명", example = "한국의 대표적인 장거리 여행 코스")
    String description,

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
    String thumbnailUrl,

    @Schema(description = "총 거리 (km)", example = "350.5")
    Double totalDistanceKm,

    @Schema(description = "난이도", example = "MEDIUM")
    String difficulty,

    @Schema(description = "카테고리", example = "DOMESTIC")
    String category,

    @Schema(description = "예상 완주 기간 (일)", example = "30")
    Integer estimatedDays,

    @Schema(description = "랜드마크 개수", example = "15")
    Integer landmarkCount,

    @Schema(description = "완주자 수", example = "1234")
    Long completedRunners
) {
    public static JourneySummaryResponse from(JourneyEntity journey, Integer landmarkCount, Long completedRunners) {
        return new JourneySummaryResponse(
            journey.getId(),
            journey.getTitle(),
            journey.getDescription(),
            journey.getThumbnailUrl(),
            journey.getTotalDistanceKm(),
            journey.getDifficulty().name(),
            journey.getCategory().name(),
            journey.getEstimatedDays(),
            landmarkCount,
            completedRunners
        );
    }
}