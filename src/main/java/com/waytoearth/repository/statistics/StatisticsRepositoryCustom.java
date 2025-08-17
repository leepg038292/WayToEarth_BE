package com.waytoearth.repository.statistics;

import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

//복잡한 쿼리용
public interface StatisticsRepositoryCustom {

    WeeklyStatsDto getWeeklyStats(Long userId, LocalDateTime start, LocalDateTime end);

    List<RunningWeeklyStatsResponse.DailyDistance> getDailyDistances(Long userId, LocalDateTime start, LocalDateTime end);
}