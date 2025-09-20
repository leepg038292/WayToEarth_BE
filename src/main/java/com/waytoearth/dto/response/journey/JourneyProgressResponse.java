package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.journey.UserJourneyProgressEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "여행 진행률 응답")
public record JourneyProgressResponse(
    @Schema(description = "진행 ID", example = "1")
    Long progressId,

    @Schema(description = "현재 누적 거리 (km)", example = "123.4")
    Double currentDistanceKm,

    @Schema(description = "진행률 (%)", example = "35.2")
    Double progressPercent,

    @Schema(description = "진행 상태", example = "ACTIVE")
    String status,

    @Schema(description = "다음 랜드마크 정보")
    LandmarkSummaryResponse nextLandmark,

    @Schema(description = "수집한 스탬프 수", example = "5")
    Integer collectedStamps,

    @Schema(description = "총 랜드마크 수", example = "15")
    Integer totalLandmarks
) {
    public static JourneyProgressResponse from(
            UserJourneyProgressEntity progress,
            LandmarkSummaryResponse nextLandmark,
            Integer collectedStamps,
            Integer totalLandmarks
    ) {
        return new JourneyProgressResponse(
            progress.getId(),
            progress.getCurrentDistanceKm(),
            progress.getProgressPercent(),
            progress.getStatus().name(),
            nextLandmark,
            collectedStamps,
            totalLandmarks
        );
    }
}