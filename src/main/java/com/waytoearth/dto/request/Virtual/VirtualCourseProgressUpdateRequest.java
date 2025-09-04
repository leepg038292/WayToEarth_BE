package com.waytoearth.dto.request.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "가상 코스 진행률 업데이트 요청 DTO")
public class VirtualCourseProgressUpdateRequest {

    @NotNull
    @Schema(description = "세그먼트 ID", example = "100")
    private Long segmentId;

    @NotNull
    @Schema(description = "추가 거리 (km)", example = "2.5")
    private Double distanceKm;

    @Schema(description = "운동 시간(초)", example = "1800")
    private Integer durationSeconds;

    @Schema(description = "평균 페이스(초/km)", example = "360")
    private Integer averagePaceSeconds;

    @Schema(description = "칼로리(kcal)", example = "350")
    private Integer calories;

    @Schema(description = "세션 ID", example = "a1b2c3d4-5678-90ef")
    private String sessionId;

    @Schema(description = "현재 위치 좌표")
    private RoutePoint currentPoint;

    @Getter
    @Setter
    @Schema(description = "경로 좌표")
    public static class RoutePoint {
        @Schema(description = "위도", example = "37.5665")
        private Double latitude;

        @Schema(description = "경도", example = "126.9780")
        private Double longitude;

        @Schema(description = "순서", example = "10")
        private Integer sequence;
    }
}
