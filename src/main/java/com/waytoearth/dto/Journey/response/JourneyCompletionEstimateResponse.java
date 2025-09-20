package com.waytoearth.dto.Journey.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "여정 완주 예상 기간 응답",
    example = """
        {
          "runsPerWeek": 3,
          "averageDistancePerRun": 5.0,
          "estimatedWeeks": 12,
          "estimatedDays": 84,
          "totalRuns": 36,
          "totalDistance": 350.5
        }
        """
)
public record JourneyCompletionEstimateResponse(
    @Schema(description = "주당 러닝 횟수", example = "3")
    Integer runsPerWeek,

    @Schema(description = "1회 평균 거리 (km)", example = "5.0")
    Double averageDistancePerRun,

    @Schema(description = "예상 완주 기간 (주)", example = "12")
    Integer estimatedWeeks,

    @Schema(description = "예상 완주 기간 (일)", example = "84")
    Integer estimatedDays,

    @Schema(description = "총 러닝 횟수", example = "36")
    Integer totalRuns,

    @Schema(description = "여정 총 거리 (km)", example = "350.5")
    Double totalDistance
) {
    public static JourneyCompletionEstimateResponse calculate(
            Double totalDistance,
            Integer runsPerWeek,
            Double averageDistancePerRun
    ) {
        int totalRuns = (int) Math.ceil(totalDistance / averageDistancePerRun);
        int estimatedWeeks = (int) Math.ceil((double) totalRuns / runsPerWeek);
        int estimatedDays = estimatedWeeks * 7;

        return new JourneyCompletionEstimateResponse(
                runsPerWeek,
                averageDistancePerRun,
                estimatedWeeks,
                estimatedDays,
                totalRuns,
                totalDistance
        );
    }
}