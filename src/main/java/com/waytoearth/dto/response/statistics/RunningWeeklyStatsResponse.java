package com.waytoearth.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "RunningWeeklyStatsResponse", description = "이번 주 러닝 통계 요약 및 요일별 거리")
public class RunningWeeklyStatsResponse {

    @Schema(description = "이번 주 총 거리(KM)", example = "29.7")
    private double totalDistance;

    @Schema(description = "이번 주 총 시간(초)", example = "13560")
    private long totalDuration;

    @Schema(description = "평균 페이스(mm:ss)", example = "07:30")
    private String averagePace;

    @Schema(description = "이번 주 총 소모 칼로리", example = "1729")
    private int totalCalories;

    @Schema(description = "요일별 합계 거리", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DailyDistance> dailyDistances;

    public RunningWeeklyStatsResponse(double totalDistance, long totalDuration,
                                      String averagePace, int totalCalories,
                                      List<DailyDistance> dailyDistances) {
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.averagePace = averagePace;
        this.totalCalories = totalCalories;
        this.dailyDistances = dailyDistances;
    }

    public double getTotalDistance() { return totalDistance; }
    public long getTotalDuration() { return totalDuration; }
    public String getAveragePace() { return averagePace; }
    public int getTotalCalories() { return totalCalories; }
    public List<DailyDistance> getDailyDistances() { return dailyDistances; }

    @Schema(name = "DailyDistance", description = "요일별 합계 거리")
    public static class DailyDistance {
        @Schema(description = "요일(영문 대문자)", example = "MONDAY")
        private String day;

        @Schema(description = "해당 요일 총 거리(KM)", example = "4.1")
        private double distance;

        public DailyDistance(String day, double distance) {
            this.day = day;
            this.distance = distance;
        }
        public String getDay() { return day; }
        public double getDistance() { return distance; }
    }
}
