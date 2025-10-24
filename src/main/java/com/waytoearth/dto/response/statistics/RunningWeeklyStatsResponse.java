package com.waytoearth.dto.response.statistics;

import java.util.List;

public class RunningWeeklyStatsResponse {

    private double totalDistance;       // 총 거리 (km)
    private long totalDuration;         // 총 시간 (초)
    private String averagePace;         // 평균 페이스 (mm:ss)
    private int totalCalories;          // 총 칼로리
    private List<DailyDistance> dailyDistances; // 요일별 거리

    public RunningWeeklyStatsResponse(double totalDistance, long totalDuration,
                                      String averagePace, int totalCalories,
                                      List<DailyDistance> dailyDistances) {
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.averagePace = averagePace;
        this.totalCalories = totalCalories;
        this.dailyDistances = dailyDistances;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public String getAveragePace() {
        return averagePace;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public List<DailyDistance> getDailyDistances() {
        return dailyDistances;
    }

    // 내부 클래스: 요일별 거리
    public static class DailyDistance {
        private String day;       // 요일명 (예: MONDAY)
        private double distance;  // 해당 요일 총 거리

        public DailyDistance(String day, double distance) {
            this.day = day;
            this.distance = distance;
        }

        public String getDay() {
            return day;
        }

        public double getDistance() {
            return distance;
        }
    }
}
