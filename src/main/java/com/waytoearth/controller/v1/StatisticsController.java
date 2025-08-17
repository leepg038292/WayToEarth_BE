package com.waytoearth.controller.v1;

import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.statistics.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/weekly")
    public ResponseEntity<RunningWeeklyStatsResponse> getWeeklyStats(@AuthUser AuthenticatedUser user) {
        RunningWeeklyStatsResponse res = statisticsService.getWeeklyStats(user);
        return ResponseEntity.ok(res);
    }
}
