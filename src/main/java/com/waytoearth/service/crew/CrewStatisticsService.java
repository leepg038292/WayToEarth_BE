package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import com.waytoearth.entity.user.User;

import java.math.BigDecimal;
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
    List<CrewRankingDto> getCrewRankingByDistance(String month, int limit);

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

    /**
     * 크루 내 멤버별 거리 랭킹 조회
     */
    List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit);

    /**
     * 크루의 월간 MVP 조회
     */
    Optional<CrewMemberRankingDto> getMvpInCrew(Long crewId, String month);

    /**
     * 동시성 안전한 통계 업데이트 (Lost Update 방지)
     */
    void updateWithMemberRunSafe(Long crewId, String month, BigDecimal memberDistance,
                                BigDecimal memberPaceSeconds, boolean isNewActiveMember);

    /**
     * 동시성 안전한 크루 멤버 수 증가/감소
     */
    boolean updateCrewMemberCountSafe(Long crewId, int delta);
}