package com.waytoearth.dto.response.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RunningCompleteResponse {

    @Schema(description = "러닝 레코드 ID", example = "456")
    private Long runningRecordId;

    @Schema(description = "총 거리(km)", example = "5.2")
    private double totalDistanceKm;

    @Schema(description = "평균 페이스(mm:ss)", example = "05:47")
    private String averagePace;

    @Schema(description = "칼로리", example = "350")
    private Integer calories;

    @Schema(description = "경로 포인트 목록")
    private List<RunningCompleteRequest.RoutePoint> routePoints;
}