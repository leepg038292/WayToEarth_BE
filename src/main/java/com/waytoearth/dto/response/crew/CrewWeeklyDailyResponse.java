package com.waytoearth.dto.response.crew;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "크루 주간/전주 일자별 거리 합계 응답")
public class CrewWeeklyDailyResponse {

    @Schema(description = "이번 주 총합 (km)")
    private final double thisWeekTotal;

    @Schema(description = "지난 주 총합 (km)")
    private final double lastWeekTotal;

    @Schema(description = "지난 주 대비 성장률(%)")
    private final Double growthRate;

    @Schema(description = "이번 주 월~일 7일의 일자별 거리 + 지난주 동일 요일 거리")
    private final List<Day> days;

    public CrewWeeklyDailyResponse(double thisWeekTotal, double lastWeekTotal, Double growthRate, List<Day> days) {
        this.thisWeekTotal = thisWeekTotal;
        this.lastWeekTotal = lastWeekTotal;
        this.growthRate = growthRate;
        this.days = days;
    }

    public double getThisWeekTotal() { return thisWeekTotal; }
    public double getLastWeekTotal() { return lastWeekTotal; }
    public Double getGrowthRate() { return growthRate; }
    public List<Day> getDays() { return days; }

    @Schema(description = "일자별 거리")
    public static class Day {
        @Schema(description = "날짜 (yyyy-MM-dd)")
        private final String date;
        @Schema(description = "요일 (e.g. MONDAY)")
        private final String dow;
        @Schema(description = "이번 주 해당일 거리(km)")
        private final double thisWeekDistance;
        @Schema(description = "지난 주 같은 요일(1주 전 해당일) 거리(km)")
        private final double lastWeekDistance;

        public Day(String date, String dow, double thisWeekDistance, double lastWeekDistance) {
            this.date = date;
            this.dow = dow;
            this.thisWeekDistance = thisWeekDistance;
            this.lastWeekDistance = lastWeekDistance;
        }

        public String getDate() { return date; }
        public String getDow() { return dow; }
        public double getThisWeekDistance() { return thisWeekDistance; }
        public double getLastWeekDistance() { return lastWeekDistance; }
    }
}

