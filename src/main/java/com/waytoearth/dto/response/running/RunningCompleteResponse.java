package com.waytoearth.dto.response.running;

import com.waytoearth.dto.response.emblem.EmblemAwardResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   // 추가
public class RunningCompleteResponse {

    @Schema(description = "러닝 기록 ID")
    private Long runningRecordId;

    @Schema(description = "러닝 제목 (수정 가능)")
    private String title;

    @Schema(description = "총 거리 (km)")
    private double totalDistanceKm;

    @Schema(description = "평균 페이스 (MM:SS)")
    private String averagePace;

    @Schema(description = "칼로리 소모량")
    private Integer calories;

    @Schema(description = "시작 시각 (ISO 8601 문자열)")
    private String startedAt;

    @Schema(description = "완료 시각 (ISO 8601 문자열)")
    private String endedAt;

    @Schema(description = "러닝 경로 좌표 목록")
    private List<RoutePoint> routePoints;

    //  추가된 필드
    @Schema(description = "엠블럼 지급 결과")
    private EmblemAwardResult emblemAwardResult;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePoint {
        private Double latitude;
        private Double longitude;
        private Integer sequence;
    }
}