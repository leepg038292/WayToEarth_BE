package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.response.crew.*;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import com.waytoearth.service.crew.CrewStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/crews/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Statistics", description = "크루 통계 관리 API")
public class CrewStatisticsController {

    private final CrewStatisticsService statisticsService;

    @Operation(summary = "크루 월간 통계 조회", description = "특정 크루의 월간 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "통계 데이터를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/monthly")
    public ResponseEntity<CrewStatisticsSummaryDto> getCrewMonthlyStatistics(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month) {

        // month가 없으면 현재 월 사용
        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        Optional<CrewStatisticsSummaryDto> statistics = statisticsService.getCrewMonthlySummary(crewId, month);

        if (statistics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(statistics.get());
    }

    @Operation(summary = "크루 전체 월간 통계 목록", description = "크루의 모든 월간 통계 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/monthly/all")
    public ResponseEntity<List<CrewStatisticsEntity>> getAllCrewMonthlyStatistics(
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        List<CrewStatisticsEntity> statistics = statisticsService.getCrewMonthlyStatistics(crewId);

        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "크루 기간별 통계 조회", description = "특정 기간 동안의 크루 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 기간 형식")
    })
    @GetMapping("/{crewId}/period")
    public ResponseEntity<List<CrewStatisticsEntity>> getCrewStatisticsByPeriod(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "시작 월 (YYYYMM 형식)", example = "202401") @RequestParam String startMonth,
            @Parameter(description = "종료 월 (YYYYMM 형식)", example = "202412") @RequestParam String endMonth) {

        List<CrewStatisticsEntity> statistics = statisticsService.getCrewStatisticsByPeriod(
                crewId, startMonth, endMonth);

        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "크루 랭킹 (거리 기준)", description = "월간 총 거리 기준으로 크루 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/rankings/distance")
    public ResponseEntity<List<CrewRankingDto>> getCrewRankingByDistance(
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month,
            @Parameter(description = "조회할 랭킹 수") @RequestParam(defaultValue = "10") int limit) {

        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        List<CrewRankingDto> ranking = statisticsService.getCrewRankingByDistance(month, limit);

        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "크루 랭킹 (러닝 횟수 기준)", description = "월간 러닝 횟수 기준으로 크루 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/rankings/runs")
    public ResponseEntity<List<CrewStatisticsSummaryDto>> getCrewRankingByRunCount(
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month,
            @Parameter(description = "조회할 랭킹 수") @RequestParam(defaultValue = "10") int limit) {

        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        List<CrewStatisticsSummaryDto> ranking = statisticsService.getCrewRankingByRunCount(month, limit);

        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "크루 랭킹 (성장률 기준)", description = "전월 대비 성장률 기준으로 크루 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/rankings/growth")
    public ResponseEntity<List<CrewStatisticsSummaryDto>> getCrewRankingByGrowth(
            @Parameter(description = "현재 월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String currentMonth,
            @Parameter(description = "이전 월 (YYYYMM 형식)", example = "202411")
            @RequestParam(required = false) String previousMonth,
            @Parameter(description = "조회할 랭킹 수") @RequestParam(defaultValue = "10") int limit) {

        if (currentMonth == null) {
            YearMonth current = YearMonth.now();
            currentMonth = current.format(DateTimeFormatter.ofPattern("yyyyMM"));
            previousMonth = current.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        List<CrewStatisticsSummaryDto> ranking = statisticsService.getCrewRankingByGrowth(
                currentMonth, previousMonth, limit);

        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "크루 내 멤버 랭킹", description = "크루 내 멤버들의 거리 기준 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members/ranking")
    public ResponseEntity<List<CrewMemberRankingDto>> getMemberRankingInCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month,
            @Parameter(description = "조회할 랭킹 수") @RequestParam(defaultValue = "20") int limit) {

        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        List<CrewMemberRankingDto> ranking = statisticsService.getMemberRankingInCrew(crewId, month, limit);

        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "크루 월간 MVP", description = "크루의 월간 MVP를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "MVP 정보 없음"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/mvp")
    public ResponseEntity<CrewMemberRankingDto> getCrewMvp(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month) {

        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        Optional<CrewMemberRankingDto> mvp = statisticsService.getMvpInCrew(crewId, month);

        if (mvp.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mvp.get());
    }

    @Operation(summary = "월간 MVP 갱신", description = "특정 크루의 월간 MVP를 수동으로 갱신합니다. (관리자용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @PostMapping("/{crewId}/mvp/refresh")
    public ResponseEntity<Void> refreshMvp(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "월 (YYYYMM 형식)", example = "202412")
            @RequestParam(required = false) String month) {

        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        log.info("MVP 수동 갱신 - crewId: {}, month: {}", crewId, month);

        statisticsService.updateMvpForMonth(crewId, month);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "새 달 통계 초기화", description = "새로운 월의 통계를 초기화합니다. (관리자용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초기화 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @PostMapping("/{crewId}/reset")
    public ResponseEntity<Void> resetStatisticsForNewMonth(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "새로운 월 (YYYYMM 형식)", example = "202501") @RequestParam String newMonth) {

        log.info("새 달 통계 초기화 - crewId: {}, newMonth: {}", crewId, newMonth);

        statisticsService.resetStatisticsForNewMonth(crewId, newMonth);

        return ResponseEntity.ok().build();
    }
}