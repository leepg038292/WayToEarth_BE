package com.waytoearth.dto.response.running;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RunningRecordSummaryResponse {

    @Schema(example = "456")
    private Long id;

    @Schema(example = "금요일 오전 러닝")
    private String title;

    @Schema(description = "거리(km)", example = "5.2")
    private double distanceKm;

    @Schema(description = "운동 시간(초)", example = "1800")
    private int durationSeconds;

    @Schema(description = "평균 페이스(문자열 mm:ss)", example = "05:47")
    private String averagePace;

    @Schema(description = "칼로리", example = "350")
    private int calories;

    @Schema(description = "시작 시각(한국시간 ISO-8601)", example = "2025-08-14T09:30:00")
    private String startedAt;

    //  추가
    @Schema(description = "러닝 타입 (SINGLE / JOURNEY)")
    private String runningType;

}
