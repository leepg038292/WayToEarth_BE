package com.waytoearth.dto.Journey.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "여행 진행률 업데이트 요청")
public record JourneyProgressUpdateRequest(
    @NotBlank(message = "세션 ID는 필수입니다")
    @Schema(description = "러닝 세션 ID", example = "session-uuid-123")
    String sessionId,

    @NotNull(message = "거리는 필수입니다")
    @DecimalMin(value = "0.0", message = "거리는 0 이상이어야 합니다")
    @Schema(description = "이번 세션에서 뛴 거리 (km)", example = "2.5")
    Double distanceKm,

    @Valid
    @Schema(description = "현재 위치")
    LocationPoint currentLocation,

    @Min(value = 0, message = "운동 시간은 0 이상이어야 합니다")
    @Schema(description = "운동 시간 (초)", example = "1800")
    Integer durationSeconds,

    @Min(value = 0, message = "칼로리는 0 이상이어야 합니다")
    @Schema(description = "소모 칼로리", example = "250")
    Integer calories,

    @Min(value = 0, message = "평균 페이스는 0 이상이어야 합니다")
    @Schema(description = "평균 페이스 (초/km)", example = "360")
    Integer averagePaceSeconds
) {
    @Schema(description = "위치 정보")
    public record LocationPoint(
        @NotNull(message = "위도는 필수입니다")
        @Schema(description = "위도", example = "37.5665")
        Double latitude,

        @NotNull(message = "경도는 필수입니다")
        @Schema(description = "경도", example = "126.9780")
        Double longitude
    ) {}
}