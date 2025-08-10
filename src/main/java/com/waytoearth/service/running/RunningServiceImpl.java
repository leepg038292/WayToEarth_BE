package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.RunningRoute;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.service.running.RunningService;
import com.waytoearth.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final UserService userService;

    @Override
    public RunningStartResponse start(String kakaoId, RunningStartRequest req) {
        User user = userService.getByKakaoId(kakaoId);

        // 유저가 이미 진행중이면 예외
        if (runningRecordRepository.existsByUserAndIsCompletedFalse(user)) {
            throw new RunningException("이미 진행 중인 러닝 세션이 있습니다.");
        }

        String sessionId = Optional.ofNullable(req.getSessionId())
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        RunningType type = RunningType.fromCode(req.getRunningType());

        RunningRecord record = RunningRecord.builder()
                .user(user)
                .sessionId(sessionId)
                .title((req.getTitle() != null && !req.getTitle().isBlank()) ? req.getTitle() : "러닝 기록")
                .runningType(type)
                // weatherCondition 설정 제거 (기록 스냅샷 미사용)
                .startedAt(LocalDateTime.now())
                .isCompleted(false)
                .build();

        runningRecordRepository.save(record);

        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public RunningCompleteResponse complete(String kakaoId, RunningCompleteRequest req) {
        User user = userService.getByKakaoId(kakaoId);

        RunningRecord record = runningRecordRepository.findBySessionId(req.getSessionId())
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RunningException("세션을 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(record.getIsCompleted())) {
            throw new RunningException("이미 완료된 세션입니다.");
        }

        // 값 반영
        record.setDistance(req.getDistance());
        record.setDuration(req.getDuration());

        String pace = req.getAveragePace();
        if (pace == null || pace.isBlank()) {
            pace = formatPace(req.getDuration(), req.getDistance());
        }
        record.setAveragePace(pace);

        Integer calories = req.getCalories();
        if (calories == null) {
            calories = estimateCalories(req.getDistance());
        }
        record.setCalories(calories);

        record.setEndedAt(LocalDateTime.now());
        record.setIsCompleted(true);

        // 경로 저장
        if (req.getRoute() != null && !req.getRoute().isEmpty()) {
            for (var p : req.getRoute()) {
                RunningRoute route = RunningRoute.builder()
                        .runningRecord(record)
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .sequence(p.getSequence() == null ? 0 : p.getSequence())
                        .timestamp(p.getTimestamp())
                        .build();
                record.getRouteData().add(route);
            }
        }

        runningRecordRepository.save(record);

        return new RunningCompleteResponse(
                record.getId(),
                record.getDistance(),
                record.getDuration(),
                record.getAveragePace(),
                record.getCalories(),
                record.getEndedAt()
        );
    }

    private String formatPace(Integer durationSec, BigDecimal distanceKm) {
        if (durationSec == null || durationSec <= 0 || distanceKm == null || distanceKm.doubleValue() <= 0) {
            return "00:00";
        }
        double paceSecPerKm = durationSec / distanceKm.doubleValue();
        int minutes = (int) (paceSecPerKm / 60);
        int seconds = (int) Math.round(paceSecPerKm % 60);
        if (seconds == 60) {
            minutes += 1;
            seconds = 0;
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    private int estimateCalories(BigDecimal distanceKm) {
        if (distanceKm == null) return 0;
        return (int) Math.round(distanceKm.doubleValue() * 60.0); // km당 60kcal 가정
    }
}