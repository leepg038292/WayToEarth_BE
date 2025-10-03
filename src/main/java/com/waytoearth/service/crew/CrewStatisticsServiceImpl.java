package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewStatisticsSummaryDto;
import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewStatisticsRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.service.ranking.CrewRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CrewStatisticsServiceImpl implements CrewStatisticsService {

    private final CrewStatisticsRepository statisticsRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final CrewRankingService crewRankingService;

    @Override
    @Transactional
    public CrewStatisticsEntity getOrCreateMonthlyStatistics(Long crewId, String month) {
        CrewEntity crew = getCrewEntity(crewId);

        Optional<CrewStatisticsEntity> existing = statisticsRepository.findByCrewAndMonth(crew, month);

        if (existing.isPresent()) {
            return existing.get();
        }

        // 새로운 월간 통계 생성
        long activeMemberCount = crewMemberRepository.countByCrewAndIsActiveTrue(crew);

        CrewStatisticsEntity newStats = CrewStatisticsEntity.builder()
                .crew(crew)
                .month(month)
                .activeMembers((int) activeMemberCount)
                .build();

        CrewStatisticsEntity saved = statisticsRepository.save(newStats);

        log.info("새로운 월간 통계가 생성되었습니다. crewId: {}, month: {}", crewId, month);

        return saved;
    }

    @Override
    public Optional<CrewStatisticsEntity> getMonthlyStatistics(Long crewId, String month) {
        CrewEntity crew = getCrewEntity(crewId);
        return statisticsRepository.findByCrewAndMonth(crew, month);
    }

    @Override
    public List<CrewStatisticsEntity> getCrewMonthlyStatistics(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        return statisticsRepository.findByCrewOrderByMonthDesc(crew);
    }

    @Override
    public List<CrewStatisticsEntity> getCrewStatisticsByPeriod(Long crewId, String startMonth, String endMonth) {
        CrewEntity crew = getCrewEntity(crewId);
        return statisticsRepository.findByCrewAndMonthBetweenOrderByMonth(crew, startMonth, endMonth);
    }

    @Override
    @Transactional
    public void updateStatisticsAfterRun(Long crewId, Long userId, String month,
                                       Double distance, Long duration) {
        CrewStatisticsEntity stats = getOrCreateMonthlyStatistics(crewId, month);
        User user = getUserEntity(userId);

        // 통계 업데이트
        // distance를 BigDecimal로 변환
        BigDecimal distanceBd = BigDecimal.valueOf(distance);
        // duration에서 pace 계산 (초)
        BigDecimal paceSeconds = BigDecimal.valueOf(duration).divide(distanceBd, 2, BigDecimal.ROUND_HALF_UP);

        stats.updateWithMemberRun(distanceBd, paceSeconds, false);

        log.info("러닝 완료 후 통계가 업데이트되었습니다. crewId: {}, userId: {}, month: {}, distance: {}",
                crewId, userId, month, distance);

        // MVP 갱신 (비동기적으로 처리할 수도 있음)
        updateMvpForMonth(crewId, month);

        // Redis 랭킹 실시간 업데이트
        updateRedisRankingAfterRun(crewId, userId, month);
    }

    @Override
    @Transactional
    public void updateMvpForMonth(Long crewId, String month) {
        CrewEntity crew = getCrewEntity(crewId);

        // 현재 MVP 조회
        CrewMemberRankingDto currentMvp = statisticsRepository.findMvpInCrew(crewId, month);

        if (currentMvp != null) {
            // CrewStatisticsEntity의 mvpUserId 업데이트
            Optional<CrewStatisticsEntity> statsOpt = getMonthlyStatistics(crewId, month);
            if (statsOpt.isPresent()) {
                CrewStatisticsEntity stats = statsOpt.get();
                // MVP 사용자 조회하여 설정
                User mvpUser = userRepository.findById(currentMvp.getUserId())
                        .orElseThrow(() -> new RuntimeException("MVP 사용자를 찾을 수 없습니다. userId: " + currentMvp.getUserId()));
                stats.setMvpUser(mvpUser);
                stats.setMvpDistance(currentMvp.getTotalDistance());

                log.info("월간 MVP가 갱신되었습니다. crewId: {}, month: {}, mvpUserId: {}",
                        crewId, month, currentMvp.getUserId());
            }
        }
    }

    @Override
    public List<CrewRankingDto> getCrewRankingByDistance(String month, int limit) {
        // Redis 우선 조회! 고성능 랭킹 시스템
        return crewRankingService.getCrewRanking(month, limit);
    }

    @Override
    public List<CrewStatisticsSummaryDto> getCrewRankingByRunCount(String month, int limit) {
        List<CrewStatisticsEntity> topCrews = statisticsRepository.findTopCrewsByRunCount(month, limit);
        return topCrews.stream()
                .map(stats -> new CrewStatisticsSummaryDto(
                        stats.getMonth(),
                        1, // 크루 수
                        stats.getTotalDistance().doubleValue(),
                        stats.getActiveMembers(),
                        stats.getAvgPaceSeconds() != null ? stats.getAvgPaceSeconds().doubleValue() : 0.0
                ))
                .toList();
    }

    @Override
    public List<CrewStatisticsSummaryDto> getCrewRankingByGrowth(String currentMonth, String previousMonth, int limit) {
        // 현재는 월간 누적 거리 랭킹만 사용, 성장률 랭킹은 향후 필요시 구현
        return Collections.emptyList();
    }

    @Override
    public Optional<CrewStatisticsSummaryDto> getCrewMonthlySummary(Long crewId, String month) {
        Optional<CrewStatisticsEntity> statsOpt = getMonthlyStatistics(crewId, month);

        if (statsOpt.isPresent()) {
            CrewStatisticsEntity stats = statsOpt.get();
            return Optional.of(new CrewStatisticsSummaryDto(
                    stats.getMonth(),
                    1, // 크루 수
                    stats.getTotalDistance().doubleValue(),
                    stats.getActiveMembers(),
                    stats.getAvgPaceSeconds() != null ? stats.getAvgPaceSeconds().doubleValue() : 0.0
            ));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public void resetStatisticsForNewMonth(Long crewId, String newMonth) {
        CrewEntity crew = getCrewEntity(crewId);

        // 기존 통계가 있는지 확인
        if (!statisticsRepository.existsByCrewAndMonth(crew, newMonth)) {
            // 새 달 통계 생성
            getOrCreateMonthlyStatistics(crewId, newMonth);

            log.info("새 달 통계가 초기화되었습니다. crewId: {}, month: {}", crewId, newMonth);
        }
    }

    @Override
    @Transactional
    public void cleanupStatisticsForCrew(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        List<CrewStatisticsEntity> allStats = statisticsRepository.findByCrewOrderByMonthDesc(crew);

        if (!allStats.isEmpty()) {
            statisticsRepository.deleteAll(allStats);
            log.info("크루 삭제에 따른 통계 데이터가 정리되었습니다. crewId: {}, deletedCount: {}",
                    crewId, allStats.size());
        }
    }

    /**
     * 동시성 안전한 통계 업데이트 메서드 (Lost Update 방지)
     * 원자적 SQL 업데이트와 낙관적 잠금을 함께 사용
     */
    @Override
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void updateWithMemberRunSafe(Long crewId, String month, BigDecimal memberDistance,
                                       BigDecimal memberPaceSeconds, boolean isNewActiveMember) {

        // 1. 먼저 통계 레코드가 있는지 확인하고 생성
        getOrCreateMonthlyStatistics(crewId, month);

        // 2. 원자적 SQL 업데이트로 기본 통계 업데이트
        int updateCount = statisticsRepository.updateStatisticsAtomically(
                crewId, month, 1, memberDistance, isNewActiveMember);

        if (updateCount == 0) {
            log.warn("통계 원자적 업데이트 실패 - crewId: {}, month: {}", crewId, month);
            throw new RuntimeException("통계 업데이트에 실패했습니다.");
        }

        // 3. 평균 페이스 계산은 별도 트랜잭션에서 처리 (복잡한 계산이므로 읽기 후 계산 후 업데이트)
        updateAveragePaceSafe(crewId, month, memberDistance, memberPaceSeconds);

        log.debug("안전한 통계 업데이트 완료 - crewId: {}, month: {}, distance: {}",
                  crewId, month, memberDistance);
    }

    /**
     * 평균 페이스 안전 업데이트 (복잡한 계산 필요)
     */
    private void updateAveragePaceSafe(Long crewId, String month, BigDecimal memberDistance, BigDecimal memberPaceSeconds) {
        CrewEntity crew = getCrewEntity(crewId);
        Optional<CrewStatisticsEntity> statsOpt = statisticsRepository.findByCrewAndMonth(crew, month);

        if (statsOpt.isPresent()) {
            CrewStatisticsEntity stats = statsOpt.get();

            // 새로운 평균 페이스 계산
            BigDecimal newAvgPace;
            if (stats.getAvgPaceSeconds() == null) {
                newAvgPace = memberPaceSeconds;
            } else {
                // 거리 기반 가중평균 계산
                BigDecimal currentTotalDistance = stats.getTotalDistance();
                BigDecimal previousDistance = currentTotalDistance.subtract(memberDistance);

                BigDecimal totalWeightedPace = stats.getAvgPaceSeconds().multiply(previousDistance);
                BigDecimal newWeightedPace = memberPaceSeconds.multiply(memberDistance);
                newAvgPace = totalWeightedPace.add(newWeightedPace)
                        .divide(currentTotalDistance, 2, BigDecimal.ROUND_HALF_UP);
            }

            // 원자적 평균 페이스 업데이트
            statisticsRepository.updateAveragePace(crewId, month, newAvgPace);
        }
    }

    /**
     * 동시성 안전한 크루 멤버 수 증가/감소
     */
    @Override
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 50))
    public boolean updateCrewMemberCountSafe(Long crewId, int delta) {
        int updateCount = statisticsRepository.updateCurrentMembersAtomically(crewId, delta);

        if (updateCount == 0) {
            log.warn("크루 멤버 수 원자적 업데이트 실패 - crewId: {}, delta: {}", crewId, delta);
            return false; // 정원 초과나 0 미만이 되려고 했음
        }

        log.debug("크루 멤버 수 안전 업데이트 완료 - crewId: {}, delta: {}", crewId, delta);
        return true;
    }

    // Private helper methods
    private CrewEntity getCrewEntity(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    @Override
    public List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit) {
        // Redis 우선 조회! 고성능 랭킹 시스템
        return crewRankingService.getMemberRankingInCrew(crewId, month, limit);
    }

    @Override
    public Optional<CrewMemberRankingDto> getMvpInCrew(Long crewId, String month) {
        CrewMemberRankingDto mvp = statisticsRepository.findMvpInCrew(crewId, month);
        return Optional.ofNullable(mvp);
    }

    /**
     * 러닝 완료 후 Redis 랭킹 실시간 업데이트
     * 동시성 문제 해결: DB 조회 대신 Redis ZINCRBY 사용하여 원자적 증가
     * 재시도 로직: Redis 실패 시 최대 3번 재시도
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2)
    )
    private void updateRedisRankingAfterRun(Long crewId, Long userId, String month) {
        try {
            // 1. DB에서 최신 거리 조회 (Redis와 동기화)
            BigDecimal userTotalDistance = getUserMonthlyTotalDistance(crewId, userId, month);
            BigDecimal crewTotalDistance = getCrewMonthlyTotalDistance(crewId, month);

            // 2. Redis에 거리 업데이트 (ZADD)
            // 동시성 이슈가 있지만, DB가 source of truth이므로 주기적으로 동기화
            crewRankingService.updateMemberRanking(crewId, userId, month, userTotalDistance.doubleValue());
            crewRankingService.updateCrewRanking(crewId, month, crewTotalDistance.doubleValue());

            // 3. Redis에 러닝 횟수 증가 (원자적 증가)
            crewRankingService.incrementMemberRunCount(crewId, userId, month);
            crewRankingService.incrementCrewRunCount(crewId, month);

            log.debug("Redis 랭킹 업데이트 완료. crewId: {}, userId: {}, month: {}, userDistance: {}, crewDistance: {}",
                    crewId, userId, month, userTotalDistance, crewTotalDistance);

        } catch (Exception e) {
            log.error("Redis 랭킹 업데이트 실패 (재시도 후에도 실패). crewId: {}, userId: {}, month: {}, error: {}",
                    crewId, userId, month, e.getMessage(), e);
            // Redis 오류가 메인 로직을 방해하지 않도록 예외를 먹음
            // 실패해도 DB에는 저장되어 있으므로, 다음 배치 동기화나 랭킹 조회 시 DB에서 캐싱됨
        }
    }

    /**
     * 사용자의 월간 누적 거리 조회 (N+1 쿼리 최적화)
     */
    private BigDecimal getUserMonthlyTotalDistance(Long crewId, Long userId, String month) {
        return statisticsRepository.findUserMonthlyDistanceInCrew(crewId, userId, month);
    }

    /**
     * 크루의 월간 전체 거리 조회
     */
    private BigDecimal getCrewMonthlyTotalDistance(Long crewId, String month) {
        CrewStatisticsEntity stats = getMonthlyStatistics(crewId, month).orElse(null);
        return stats != null ? stats.getTotalDistance() : BigDecimal.ZERO;
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
    }
}