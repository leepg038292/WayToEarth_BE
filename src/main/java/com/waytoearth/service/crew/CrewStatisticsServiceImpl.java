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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        stats.updateWithNewRun(distanceBd, paceSeconds, 1);

        log.info("러닝 완료 후 통계가 업데이트되었습니다. crewId: {}, userId: {}, month: {}, distance: {}",
                crewId, userId, month, distance);

        // MVP 갱신 (비동기적으로 처리할 수도 있음)
        updateMvpForMonth(crewId, month);
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
                stats.setMvpUserId(currentMvp.getUserId());

                log.info("월간 MVP가 갱신되었습니다. crewId: {}, month: {}, mvpUserId: {}",
                        crewId, month, currentMvp.getUserId());
            }
        }
    }

    @Override
    public List<CrewRankingDto> getCrewRankingByDistance(String month, int limit) {
        List<CrewRankingDto> ranking = statisticsRepository.findCrewRankingByActualDistance(month, limit);

        // 랭킹 순위 설정 (1, 2, 3, ...)
        for (int i = 0; i < ranking.size(); i++) {
            CrewRankingDto crew = ranking.get(i);
            CrewRankingDto withRank = new CrewRankingDto(
                    crew.getMonth(),
                    crew.getCrewId(),
                    crew.getCrewName(),
                    crew.getTotalDistance(),
                    crew.getRunCount(),
                    i + 1 // 랭킹은 1부터 시작
            );
            ranking.set(i, withRank);
        }

        return ranking;
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
        // TODO: 성장률 랭킹은 추후 구현
        return List.of();
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

    // Private helper methods
    private CrewEntity getCrewEntity(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    @Override
    public List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit) {
        List<CrewMemberRankingDto> ranking = statisticsRepository.findMemberRankingInCrew(crewId, month, limit);

        // 랭킹 순위 설정 (1, 2, 3, ...)
        for (int i = 0; i < ranking.size(); i++) {
            // CrewMemberRankingDto는 immutable이므로 새 객체 생성
            CrewMemberRankingDto member = ranking.get(i);
            CrewMemberRankingDto withRank = new CrewMemberRankingDto(
                    member.getMonth(),
                    member.getUserId(),
                    member.getUserName(),
                    member.getTotalDistance(),
                    member.getRunCount(),
                    i + 1 // 랭킹은 1부터 시작
            );
            ranking.set(i, withRank);
        }

        return ranking;
    }

    @Override
    public Optional<CrewMemberRankingDto> getMvpInCrew(Long crewId, String month) {
        CrewMemberRankingDto mvp = statisticsRepository.findMvpInCrew(crewId, month);
        return Optional.ofNullable(mvp);
    }

    // Private helper methods
    private CrewEntity getCrewEntity(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
    }
}