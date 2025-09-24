package com.waytoearth.repository.crew;

import com.waytoearth.dto.response.crew.CrewGrowthDto;
import com.waytoearth.dto.response.crew.CrewRankingComparisonDto;
import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;

import java.util.List;

public interface CrewStatisticsRepositoryCustom {

    /**
     * 월별 상위 크루 조회 (거리 기준)
     */
    List<CrewStatisticsEntity> findTopCrewsByDistance(String month, int limit);

    /**
     * 월별 상위 크루 조회 (활동 멤버 기준)
     */
    List<CrewStatisticsEntity> findTopCrewsByActiveMembers(String month, int limit);

    /**
     * 크루 성장률 계산 (이번달 vs 지난달)
     */
    CrewGrowthDto calculateCrewGrowth(Long crewId, String currentMonth, String previousMonth);

    /**
     * 통계 계산이 필요한 크루들 조회
     */
    List<CrewEntity> findCrewsNeedingStatsCalculation(String month);

    /**
     * 크루별 월간 랭킹 비교
     */
    List<CrewRankingComparisonDto> getCrewRankingComparison(List<Long> crewIds, String month);

    /**
     * 전체 크루 통계 요약
     */
    CrewStatisticsSummaryDto getOverallStatistics(String month);

}