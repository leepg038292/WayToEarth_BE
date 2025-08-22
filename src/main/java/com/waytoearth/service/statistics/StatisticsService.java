package com.waytoearth.service.statistics;

import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;
import com.waytoearth.security.AuthenticatedUser;

public interface StatisticsService {
    RunningWeeklyStatsResponse getWeeklyStats(AuthenticatedUser user);
}