package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.repository.crew.CrewMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 크루 통계 업데이트 전용 컴포넌트 (재시도 로직 포함)
 * @Retryable이 프록시 기반으로 작동하기 위해 별도 컴포넌트로 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrewStatisticsUpdater {

    private final CrewMemberRepository crewMemberRepository;
    private final CrewStatisticsService crewStatisticsService;

    /**
     * 사용자가 크루 멤버인 경우 크루 통계 업데이트 (재시도 로직 포함)
     * - 크루 월간 통계 (거리, 횟수, 페이스) 업데이트
     * - 크루 MVP 갱신
     * - Redis 랭킹 실시간 업데이트
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void updateCrewStatisticsIfMember(Long userId, Double distanceKm, Integer durationSeconds) {
        // 사용자가 속한 활성 크루 조회
        List<CrewMemberEntity> memberships = crewMemberRepository.findByUserIdWithCrew(userId);

        if (memberships.isEmpty()) {
            return; // 크루 미가입 사용자는 스킵
        }

        String month = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        for (CrewMemberEntity membership : memberships) {
            if (membership.getIsActive() && membership.getCrew().getIsActive()) {
                crewStatisticsService.updateStatisticsAfterRun(
                        membership.getCrew().getId(),
                        userId,
                        month,
                        distanceKm,
                        durationSeconds.longValue()
                );

                log.info("크루 통계 업데이트 완료: crewId={}, userId={}, distance={}km",
                        membership.getCrew().getId(), userId, distanceKm);
            }
        }
    }

    /**
     * 크루 통계 업데이트 재시도 실패 시 복구 메서드
     */
    @Recover
    public void recoverCrewStatisticsUpdate(Exception e, Long userId, Double distanceKm, Integer durationSeconds) {
        // 최종 실패 시에도 러닝 완료는 성공 처리
        log.error("크루 통계 업데이트 최종 실패 (3회 재시도 후): userId={}, distance={}km, error={}",
                userId, distanceKm, e.getMessage(), e);
        // TODO: 실패한 업데이트를 별도 큐에 저장하여 배치로 재처리 고려
    }
}
