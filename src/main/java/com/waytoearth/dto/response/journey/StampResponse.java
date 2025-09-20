package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.journey.StampEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "스탬프 응답")
public record StampResponse(
    @Schema(description = "스탬프 ID", example = "1")
    Long id,

    @Schema(description = "랜드마크 정보")
    LandmarkSummaryResponse landmark,

    @Schema(description = "수집 시간", example = "2024-01-15T14:30:00")
    LocalDateTime collectedAt,

    @Schema(description = "스탬프 이미지 URL", example = "https://example.com/stamp.png")
    String stampImageUrl
) {
    public static StampResponse from(StampEntity stamp) {
        return new StampResponse(
            stamp.getId(),
            LandmarkSummaryResponse.from(stamp.getLandmark()),
            stamp.getCollectedAt(),
            stamp.getStampImageUrl()
        );
    }
}