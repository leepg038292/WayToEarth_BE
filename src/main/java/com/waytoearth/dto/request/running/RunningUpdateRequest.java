package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class RunningUpdateRequest {
    @Schema(description = "세션 ID", example = "abc123")
    private String sessionId;

    @Schema(description = "누적 이동 거리(미터)", example = "1500")
    private double distanceMeters;

    @Schema(description = "누적 운동 시간(초)", example = "420")
    private int durationSeconds;

    @Schema(description = "평균 페이스(초/킬로)", example = "280")
    private int averagePaceSeconds;

    @Schema(description = "소모 칼로리", example = "110")
    private int calories;

    @Schema(description = "현재 좌표 포인트")
    private PointDto currentPoint;

    @Getter @Setter
    public static class PointDto {
        @Schema(example = "37.5665")
        private double latitude;
        @Schema(example = "126.9780")
        private double longitude;
        @Schema(description = "경로 순번(0부터)", example = "15")
        private int sequence;
    }
}