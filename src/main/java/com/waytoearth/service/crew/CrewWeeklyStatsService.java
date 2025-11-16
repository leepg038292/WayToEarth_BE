package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewWeeklyCompareResponse;

import java.time.LocalDate;

public interface CrewWeeklyStatsService {

    CrewWeeklyCompareResponse getWeeklyCompare(Long crewId, LocalDate weekStart, int limit);

    com.waytoearth.dto.response.crew.CrewWeeklyDailyResponse getWeeklyDaily(Long crewId, LocalDate weekStart);
}
