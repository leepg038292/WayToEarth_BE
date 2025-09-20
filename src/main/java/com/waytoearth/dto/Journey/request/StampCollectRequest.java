package com.waytoearth.dto.Journey.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스탬프 수집 요청")
public record StampCollectRequest(
    @NotNull(message = "진행 ID는 필수입니다")
    @Schema(description = "여행 진행 ID", example = "1")
    Long progressId,

    @NotNull(message = "랜드마크 ID는 필수입니다")
    @Schema(description = "랜드마크 ID", example = "1")
    Long landmarkId,

    @Valid
    @Schema(description = "수집 위치")
    LocationPoint collectionLocation
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