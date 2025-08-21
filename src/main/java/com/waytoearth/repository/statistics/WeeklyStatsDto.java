package com.waytoearth.repository.statistics;

import lombok.Getter;

@Getter
public class WeeklyStatsDto {
    // Getter Methods
    private final Double totalDistance;
    private final Long totalDuration;
    private final Double averagePaceSeconds; // 평균 페이스 초 단위
    private final Integer totalCalories;

    // 생성자 수정
    public WeeklyStatsDto(Double totalDistance, Long totalDuration,
                          Double averagePaceSeconds, Integer totalCalories) {
        this.totalDistance = totalDistance == null ? 0.0 : totalDistance;
        this.totalDuration = totalDuration == null ? 0L : totalDuration;
        this.averagePaceSeconds = averagePaceSeconds == null ? 0.0 : averagePaceSeconds;
        this.totalCalories = totalCalories == null ? 0 : totalCalories;
    }

}