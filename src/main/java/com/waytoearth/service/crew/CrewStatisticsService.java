package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import com.waytoearth.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface CrewStatisticsService {

    /**
     * 크루의 월간 통계 조회 (없으면 생성)
     */
    CrewStatisticsEntity getOrCreateMonthlyStatistics(Long crewId, String month);

    /**
     * 크루의 월간 통계 조회 (존재하는 경우만)
     */
    Optional<CrewStatisticsEntity> getMonthlyStatistics(Long crewId, String month);

    /**
     * 크루의 전체 월간 통계 목록
     */
    List<CrewStatisticsEntity> getCrewMonthlyStatistics(Long crewId);

    /**
     * 크루의 기간별 통계 목록
     */
    List<CrewStatisticsEntity> getCrewStatisticsByPeriod(Long crewId, String startMonth, String endMonth);

    /**
     * 러닝 완료 시 통계 업데이트
     */
    void updateStatisticsAfterRun(Long crewId, Long userId, String month,
                                 Double distance, Long duration);

    /**
     * 월간 MVP 갱신
     */
    void updateMvpForMonth(Long crewId, String month);

    /**
     * 크루 랭킹 조회 (거리 기준)
     */
    List<CrewStatisticsSummaryDto> getCrewRankingByDistance(String month, int limit);

    /**
     * 크루 랭킹 조회 (러닝 횟수 기준)
     */
    List<CrewStatisticsSummaryDto> getCrewRankingByRunCount(String month, int limit);

    /**
     * 성장률 기준 크루 랭킹
     */
    List<CrewStatisticsSummaryDto> getCrewRankingByGrowth(String currentMonth, String previousMonth, int limit);

    /**
     * 특정 크루의 월간 통계 요약
     */
    Optional<CrewStatisticsSummaryDto> getCrewMonthlySummary(Long crewId, String month);

    /**
     * 새 달 시작 시 통계 리셋
     */
    void resetStatisticsForNewMonth(Long crewId, String newMonth);

    /**
     * 크루 삭제 시 관련 통계 정리
     */
    void cleanupStatisticsForCrew(Long crewId);
}