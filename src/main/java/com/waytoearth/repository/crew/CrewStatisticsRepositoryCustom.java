package com.waytoearth.repository.crew;

import com.waytoearth.dto.response.crew.CrewGrowthDto;
import com.waytoearth.dto.response.crew.CrewRankingComparisonDto;
import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;

import java.util.List;

public interface CrewStatisticsRepositoryCustom {

    /**
     * 월별 상위 크루 조회 (총 거리 기준)
     */
    List<CrewStatisticsEntity> findTopCrewsByDistance(String month, int limit);

    /**
     * 월별 상위 크루 조회 (러닝 횟수 기준)
     */
    List<CrewStatisticsEntity> findTopCrewsByRunCount(String month, int limit);

    /**
     * 월별 상위 크루 조회 (활동 멤버 기준)
     */
    List<CrewStatisticsEntity> findTopCrewsByActiveMembers(String month, int limit);

    /**
     * 크루 성장률 계산 (이번달 vs 지난달)
     */
    CrewGrowthDto calculateCrewGrowth(Long crewId, String currentMonth, String previousMonth);

    /**
     * 해당 월 통계가 없는 크루들 조회
     */
    List<CrewEntity> findCrewsNeedingStatsCalculation(String month);

    /**
     * 크루별 월간 랭킹 비교
     */
    List<CrewRankingComparisonDto> getCrewRankingComparison(List<Long> crewIds, String month);

    /**
     * 월별 전체 크루 통계 요약
     */
    CrewStatisticsSummaryDto getOverallStatistics(String month);

    /**
     * 실제 러닝 데이터 기반 크루 거리 랭킹 (RunningRecord 집계)
     */
    List<CrewRankingDto> findCrewRankingByActualDistance(String month, int limit);

    /**
     * 크루 내 멤버별 월간 거리 랭킹 (RunningRecord 기반)
     */
    List<CrewMemberRankingDto> findMemberRankingInCrew(Long crewId, String month, int limit);

    /**
     * 크루의 월간 MVP 조회 (거리 기준, RunningRecord 기반)
     */
    CrewMemberRankingDto findMvpInCrew(Long crewId, String month);

}