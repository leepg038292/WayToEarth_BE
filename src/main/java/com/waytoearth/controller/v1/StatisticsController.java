package com.waytoearth.controller.v1;

import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Statistics", description = "러닝 통계 API")
@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(
            summary = "주간 통계 조회",
            description = "로그인한 사용자 기준으로 이번 주(월~일) 러닝 통계를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")  // SwaggerConfig의 보안 스키마 이름과 일치
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    schema = @Schema(implementation = RunningWeeklyStatsResponse.class),
                    examples = @ExampleObject(name = "success",
                            value = """
                            {
                              "totalDistance": 29.7,
                              "totalDuration": 13560,
                              "averagePace": "07:30",
                              "totalCalories": 1729,
                              "dailyDistances": [
                                {"day":"MONDAY","distance":4.1},
                                {"day":"TUESDAY","distance":6.0},
                                {"day":"WEDNESDAY","distance":8.2},
                                {"day":"THURSDAY","distance":5.4},
                                {"day":"FRIDAY","distance":3.2},
                                {"day":"SATURDAY","distance":2.8},
                                {"day":"SUNDAY","distance":0.0}
                              ]
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (JWT 누락/만료)",
            content = @Content(mediaType = "application/json")
    )
    @ApiResponse(
            responseCode = "403",
            description = "권한 부족",
            content = @Content(mediaType = "application/json")
    )
    @GetMapping("/weekly")
    public ResponseEntity<RunningWeeklyStatsResponse> getWeeklyStats(@AuthUser AuthenticatedUser user) {
        RunningWeeklyStatsResponse res = statisticsService.getWeeklyStats(user);
        return ResponseEntity.ok(res);
    }
}
