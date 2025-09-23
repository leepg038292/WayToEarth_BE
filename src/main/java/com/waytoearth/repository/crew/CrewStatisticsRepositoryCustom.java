package com.waytoearth.repository.crew;

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

    /**
     * DTO 클래스들
     */
    class CrewGrowthDto {
        private final Long crewId;
        private final String currentMonth;
        private final String previousMonth;
        private final Double distanceGrowthRate;
        private final Integer memberGrowthCount;
        private final Double paceImprovement;

        public CrewGrowthDto(Long crewId, String currentMonth, String previousMonth,
                           Double distanceGrowthRate, Integer memberGrowthCount, Double paceImprovement) {
            this.crewId = crewId;
            this.currentMonth = currentMonth;
            this.previousMonth = previousMonth;
            this.distanceGrowthRate = distanceGrowthRate;
            this.memberGrowthCount = memberGrowthCount;
            this.paceImprovement = paceImprovement;
        }

        // getters
        public Long getCrewId() { return crewId; }
        public String getCurrentMonth() { return currentMonth; }
        public String getPreviousMonth() { return previousMonth; }
        public Double getDistanceGrowthRate() { return distanceGrowthRate; }
        public Integer getMemberGrowthCount() { return memberGrowthCount; }
        public Double getPaceImprovement() { return paceImprovement; }
    }

    class CrewRankingComparisonDto {
        private final Long crewId;
        private final String crewName;
        private final Integer currentRank;
        private final Integer previousRank;
        private final Integer rankChange;

        public CrewRankingComparisonDto(Long crewId, String crewName,
                                      Integer currentRank, Integer previousRank, Integer rankChange) {
            this.crewId = crewId;
            this.crewName = crewName;
            this.currentRank = currentRank;
            this.previousRank = previousRank;
            this.rankChange = rankChange;
        }

        // getters
        public Long getCrewId() { return crewId; }
        public String getCrewName() { return crewName; }
        public Integer getCurrentRank() { return currentRank; }
        public Integer getPreviousRank() { return previousRank; }
        public Integer getRankChange() { return rankChange; }
    }

    class CrewStatisticsSummaryDto {
        private final String month;
        private final Integer totalCrews;
        private final Integer activeCrews;
        private final Double totalDistance;
        private final Integer totalActiveMembers;
        private final Double averagePace;

        public CrewStatisticsSummaryDto(String month, Integer totalCrews, Integer activeCrews,
                                      Double totalDistance, Integer totalActiveMembers, Double averagePace) {
            this.month = month;
            this.totalCrews = totalCrews;
            this.activeCrews = activeCrews;
            this.totalDistance = totalDistance;
            this.totalActiveMembers = totalActiveMembers;
            this.averagePace = averagePace;
        }

        // getters
        public String getMonth() { return month; }
        public Integer getTotalCrews() { return totalCrews; }
        public Integer getActiveCrews() { return activeCrews; }
        public Double getTotalDistance() { return totalDistance; }
        public Integer getTotalActiveMembers() { return totalActiveMembers; }
        public Double getAveragePace() { return averagePace; }
    }
}