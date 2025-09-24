package com.waytoearth.repository.crew;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.dto.response.crew.CrewGrowthDto;
import com.waytoearth.dto.response.crew.CrewRankingComparisonDto;
import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.waytoearth.entity.crew.QCrewEntity.crewEntity;
import static com.waytoearth.entity.crew.QCrewStatisticsEntity.crewStatisticsEntity;

@Repository
@RequiredArgsConstructor
public class CrewStatisticsRepositoryImpl implements CrewStatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CrewStatisticsEntity> findTopCrewsByDistance(String month, int limit) {
        return queryFactory
                .selectFrom(crewStatisticsEntity)
                .where(crewStatisticsEntity.month.eq(month))
                .orderBy(crewStatisticsEntity.totalDistance.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<CrewStatisticsEntity> findTopCrewsByActiveMembers(String month, int limit) {
        return queryFactory
                .selectFrom(crewStatisticsEntity)
                .where(crewStatisticsEntity.month.eq(month))
                .orderBy(crewStatisticsEntity.monthlyActiveMembers.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public CrewGrowthDto calculateCrewGrowth(Long crewId, String currentMonth, String previousMonth) {
        // 현재 달 통계
        CrewStatisticsEntity currentStats = queryFactory
                .selectFrom(crewStatisticsEntity)
                .where(crewStatisticsEntity.crew.id.eq(crewId)
                        .and(crewStatisticsEntity.month.eq(currentMonth)))
                .fetchOne();

        // 이전 달 통계
        CrewStatisticsEntity previousStats = queryFactory
                .selectFrom(crewStatisticsEntity)
                .where(crewStatisticsEntity.crew.id.eq(crewId)
                        .and(crewStatisticsEntity.month.eq(previousMonth)))
                .fetchOne();

        if (currentStats == null) {
            return new CrewGrowthDto(crewId, currentMonth, previousMonth, 0.0, 0, 0.0);
        }

        // 성장률 계산
        Double distanceGrowthRate = 0.0;
        Integer memberGrowthCount = 0;
        Double paceImprovement = 0.0;

        if (previousStats != null) {
            // 거리 성장률 계산 (%)
            if (previousStats.getTotalDistance().doubleValue() > 0) {
                distanceGrowthRate = ((currentStats.getTotalDistance().doubleValue() -
                                    previousStats.getTotalDistance().doubleValue()) /
                                    previousStats.getTotalDistance().doubleValue()) * 100;
            }

            // 멤버 증가 수
            memberGrowthCount = currentStats.getMonthlyActiveMembers() - previousStats.getMonthlyActiveMembers();

            // 페이스 변화 (양수: 느려짐, 음수: 빨라짐)
            if (previousStats.getAvgPaceSeconds() != null && currentStats.getAvgPaceSeconds() != null) {
                paceImprovement = currentStats.getAvgPaceSeconds().doubleValue() -
                                previousStats.getAvgPaceSeconds().doubleValue();
            }
        }

        return new CrewGrowthDto(crewId, currentMonth, previousMonth,
                               distanceGrowthRate, memberGrowthCount, paceImprovement);
    }

    @Override
    public List<CrewEntity> findCrewsNeedingStatsCalculation(String month) {
        // 해당 월 통계가 없는 크루들을 찾음
        return queryFactory
                .selectFrom(crewEntity)
                .where(crewEntity.isActive.isTrue()
                        .and(crewEntity.id.notIn(
                                queryFactory
                                        .select(crewStatisticsEntity.crew.id)
                                        .from(crewStatisticsEntity)
                                        .where(crewStatisticsEntity.month.eq(month))
                        )))
                .fetch();
    }

    @Override
    public List<CrewRankingComparisonDto> getCrewRankingComparison(List<Long> crewIds, String month) {
        // 현재 달 랭킹 계산 (거리 기준)
        List<Long> currentRanking = queryFactory
                .select(crewStatisticsEntity.crew.id)
                .from(crewStatisticsEntity)
                .where(crewStatisticsEntity.month.eq(month)
                        .and(crewStatisticsEntity.crew.id.in(crewIds)))
                .orderBy(crewStatisticsEntity.totalDistance.desc())
                .fetch();

        // 이전 달 계산 (202412 -> 202411)
        String previousMonth = calculatePreviousMonth(month);
        List<Long> previousRanking = queryFactory
                .select(crewStatisticsEntity.crew.id)
                .from(crewStatisticsEntity)
                .where(crewStatisticsEntity.month.eq(previousMonth)
                        .and(crewStatisticsEntity.crew.id.in(crewIds)))
                .orderBy(crewStatisticsEntity.totalDistance.desc())
                .fetch();

        // 랭킹 비교 DTO 생성
        return currentRanking.stream()
                .map(crewId -> {
                    int currentRank = currentRanking.indexOf(crewId) + 1;
                    int previousRank = previousRanking.indexOf(crewId) + 1;
                    if (previousRank == 0) previousRank = -1; // 이전 달 데이터 없음

                    int rankChange = previousRank > 0 ? previousRank - currentRank : 0;

                    String crewName = queryFactory
                            .select(crewEntity.name)
                            .from(crewEntity)
                            .where(crewEntity.id.eq(crewId))
                            .fetchOne();

                    return new CrewRankingComparisonDto(crewId, crewName,
                                                      currentRank, previousRank, rankChange);
                })
                .toList();
    }

    @Override
    public CrewStatisticsSummaryDto getOverallStatistics(String month) {
        return queryFactory
                .select(Projections.constructor(
                        CrewStatisticsSummaryDto.class,
                        Expressions.constant(month),
                        crewStatisticsEntity.crew.id.countDistinct().intValue(),
                        crewStatisticsEntity.totalDistance.sum(),
                        crewStatisticsEntity.monthlyActiveMembers.sum(),
                        crewStatisticsEntity.avgPaceSeconds.avg()
                ))
                .from(crewStatisticsEntity)
                .where(crewStatisticsEntity.month.eq(month))
                .fetchOne();
    }

    private String calculatePreviousMonth(String month) {
        int year = Integer.parseInt(month.substring(0, 4));
        int monthNum = Integer.parseInt(month.substring(4, 6));

        if (monthNum == 1) {
            year--;
            monthNum = 12;
        } else {
            monthNum--;
        }

        return String.format("%04d%02d", year, monthNum);
    }
}