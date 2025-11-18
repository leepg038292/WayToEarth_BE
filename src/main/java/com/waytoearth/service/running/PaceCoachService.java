package com.waytoearth.service.running;

import com.waytoearth.dto.response.running.PaceCoachCheckResponse;
import com.waytoearth.entity.running.RunningRecord;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.running.RunningRecordRepository;
import com.waytoearth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 실시간 페이스 코치 서비스
 * - 러닝 중 km마다 페이스 체크
 * - 평균보다 느릴 때 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaceCoachService {

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    private static final int MINIMUM_RECORDS_REQUIRED = 5;
    private static final int RECENT_RECORDS_LIMIT = 5;

    /**
     * 페이스 코치 체크
     * - 실시간으로 현재 페이스를 평균과 비교
     *
     * @param userId              사용자 ID
     * @param sessionId           러닝 세션 ID
     * @param currentKm           현재 통과한 km
     * @param currentPaceSeconds  현재 페이스 (초/km)
     * @return 페이스 체크 결과
     */
    @Transactional(readOnly = true)
    public PaceCoachCheckResponse checkPace(Long userId, String sessionId,
                                             Integer currentKm, Integer currentPaceSeconds) {
        log.info("Pace coach check - userId: {}, km: {}, pace: {}s", userId, currentKm, currentPaceSeconds);

        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 페이스 코치 활성화 여부 확인
        if (!Boolean.TRUE.equals(user.getIsPaceCoachEnabled())) {
            log.debug("Pace coach disabled for user: {}", userId);
            return PaceCoachCheckResponse.builder()
                    .coachEnabled(false)
                    .message("페이스 코치가 비활성화되어 있습니다")
                    .build();
        }

        // 3. 완료된 러닝 기록 수 확인
        long completedCount = getCompletedRecordsCount(user);
        log.debug("Completed records count: {}", completedCount);

        if (completedCount < MINIMUM_RECORDS_REQUIRED) {
            return PaceCoachCheckResponse.builder()
                    .coachEnabled(true)
                    .isAvailable(false)
                    .minimumRecordsRequired(MINIMUM_RECORDS_REQUIRED)
                    .currentRecords((int) completedCount)
                    .message(String.format("페이스 코치는 %d회 이상 러닝 후 사용 가능합니다 (현재: %d회)",
                            MINIMUM_RECORDS_REQUIRED, completedCount))
                    .build();
        }

        // 4. 최근 N회 평균 페이스 계산
        Integer averagePace = calculateRecentAveragePace(userId, RECENT_RECORDS_LIMIT);
        if (averagePace == null || averagePace == 0) {
            log.warn("Cannot calculate average pace for user: {}", userId);
            return PaceCoachCheckResponse.builder()
                    .coachEnabled(true)
                    .isAvailable(false)
                    .message("평균 페이스를 계산할 수 없습니다")
                    .build();
        }

        // 5. 현재 페이스와 비교
        int difference = currentPaceSeconds - averagePace;
        log.debug("Pace comparison - average: {}s, current: {}s, difference: {}s",
                averagePace, currentPaceSeconds, difference);

        // 6. 느리면 알림 필요
        if (difference > 0) {
            // 평균보다 느림
            String alertMessage = String.format("평균보다 %d초 느려요! 조금만 더 힘내세요!", difference);
            return PaceCoachCheckResponse.builder()
                    .coachEnabled(true)
                    .isAvailable(true)
                    .shouldAlert(true)
                    .referencePaceSeconds(averagePace)
                    .referencePaceFormatted(formatPace(averagePace))
                    .currentPaceSeconds(currentPaceSeconds)
                    .currentPaceFormatted(formatPace(currentPaceSeconds))
                    .differenceSeconds(difference)
                    .alertMessage(alertMessage)
                    .build();
        } else {
            // 평균보다 빠르거나 같음 - 알림 없음
            return PaceCoachCheckResponse.builder()
                    .coachEnabled(true)
                    .isAvailable(true)
                    .shouldAlert(false)
                    .referencePaceSeconds(averagePace)
                    .referencePaceFormatted(formatPace(averagePace))
                    .currentPaceSeconds(currentPaceSeconds)
                    .currentPaceFormatted(formatPace(currentPaceSeconds))
                    .differenceSeconds(difference)
                    .build();
        }
    }

    /**
     * 완료된 러닝 기록 수 조회
     */
    private long getCompletedRecordsCount(User user) {
        return runningRecordRepository.countByUserAndIsCompletedTrue(user);
    }

    /**
     * 최근 N회 평균 페이스 계산
     *
     * @param userId 사용자 ID
     * @param limit  최근 기록 개수
     * @return 평균 페이스 (초/km), 계산 불가 시 null
     */
    private Integer calculateRecentAveragePace(Long userId, int limit) {
        List<RunningRecord> recentRecords = runningRecordRepository
                .findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(userId)
                .stream()
                .limit(limit)
                .toList();

        if (recentRecords.isEmpty()) {
            return null;
        }

        double averagePace = recentRecords.stream()
                .map(RunningRecord::getAveragePaceSeconds)
                .filter(pace -> pace != null && pace > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return averagePace > 0 ? (int) Math.round(averagePace) : null;
    }

    /**
     * 페이스를 "분:초/km" 형식으로 변환
     *
     * @param paceSeconds 페이스 (초/km)
     * @return 포맷된 문자열 (예: "6:20/km")
     */
    private String formatPace(Integer paceSeconds) {
        if (paceSeconds == null || paceSeconds == 0) {
            return "측정 안됨";
        }
        int minutes = paceSeconds / 60;
        int seconds = paceSeconds % 60;
        return String.format("%d:%02d/km", minutes, seconds);
    }
}
