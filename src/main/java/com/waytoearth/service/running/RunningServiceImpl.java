package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    // -------------------- start --------------------
    @Override
    @Transactional
    public RunningStartResponse startRunning(Long userId, RunningStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (runningRecordRepository.existsBySessionIdAndIsCompletedFalse(request.getSessionId())) {
            throw new InvalidParameterException("이미 진행 중인 세션입니다. sessionId=" + request.getSessionId());
        }

        RunningRecord record = RunningRecord.builder()
                .user(user)
                .sessionId(request.getSessionId())
                .runningType(request.getRunningType())
                .weatherCondition(request.getWeatherCondition())
                .startedAt(LocalDateTime.now())
                .isCompleted(false)
                .status(RunningStatus.RUNNING)
                .build();

        runningRecordRepository.save(record);

        return RunningStartResponse.builder()
                .sessionId(record.getSessionId())
                .startedAt(record.getStartedAt())
                .build();
    }

    // -------------------- pause --------------------
    @Override
    @Transactional
    public RunningPauseResponse pauseRunning(Long userId, RunningPauseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new InvalidParameterException("세션을 찾을 수 없습니다. sessionId=" + request.getSessionId()));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new InvalidParameterException("해당 세션에 대한 권한이 없습니다.");
        }
        if (record.getStatus() == RunningStatus.COMPLETED) {
            throw new InvalidParameterException("이미 완료된 세션입니다.");
        }

        if (request.getDistanceMeters() == null || request.getDistanceMeters() < 0) {
            throw new InvalidParameterException("distanceMeters는 0 이상이어야 합니다.");
        }
        if (request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
            throw new InvalidParameterException("durationSeconds는 0보다 커야 합니다.");
        }

        // 스냅샷 계산
        int paceSec = calcPaceSecondsPerKm(request.getDistanceMeters(), request.getDurationSeconds());
        int calories = estimateCalories(request.getDistanceMeters());

        // 진행 누적치 반영 (완료 아님)
        record.setDistance(metersToKm(request.getDistanceMeters()));
        record.setDuration(request.getDurationSeconds());
        record.setAveragePaceSeconds(paceSec);
        record.setCalories(calories);
        record.setStatus(RunningStatus.PAUSED);

        if (request.getRoutePoints() != null && !request.getRoutePoints().isEmpty()) {
            request.getRoutePoints().forEach(p ->
                    record.addRoutePoint(p.getLatitude(), p.getLongitude(), p.getSequence())
            );
        }

        runningRecordRepository.save(record);

        return RunningPauseResponse.builder()
                .sessionId(record.getSessionId())
                .distanceMeters(request.getDistanceMeters())
                .durationSeconds(request.getDurationSeconds())
                .averagePaceSeconds(paceSec)
                .calories(calories)
                .pausedAt(LocalDateTime.now())
                .build();
    }

    // -------------------- resume --------------------
    @Override
    @Transactional
    public RunningResumeResponse resumeRunning(Long userId, RunningResumeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new InvalidParameterException("세션을 찾을 수 없습니다. sessionId=" + request.getSessionId()));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new InvalidParameterException("해당 세션에 대한 권한이 없습니다.");
        }
        if (record.getStatus() == RunningStatus.COMPLETED) {
            throw new InvalidParameterException("이미 완료된 세션입니다.");
        }

        record.setStatus(RunningStatus.RUNNING);
        runningRecordRepository.save(record);

        return RunningResumeResponse.builder()
                .sessionId(record.getSessionId())
                .resumedAt(LocalDateTime.now())
                .build();
    }

    // -------------------- complete --------------------
    @Override
    @Transactional
    public RunningCompleteResponse completeRunning(Long userId, RunningCompleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new InvalidParameterException("세션을 찾을 수 없습니다. sessionId=" + request.getSessionId()));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new InvalidParameterException("해당 세션에 대한 권한이 없습니다.");
        }

        // 멱등 처리(이미 완료)
        if (Boolean.TRUE.equals(record.getIsCompleted())) {
            return RunningCompleteResponse.builder()
                    .runningRecordId(record.getId())
                    .totalDistanceMeters(kmToMeters(record.getDistance()))
                    .durationSeconds(record.getDuration())
                    .averagePaceSeconds(nvl(record.getAveragePaceSeconds()))
                    .calories(nvl(record.getCalories()))
                    .endedAt(record.getEndedAt())
                    .build();
        }

        if (request.getDistanceMeters() == null || request.getDistanceMeters() < 0) {
            throw new InvalidParameterException("distanceMeters는 0 이상이어야 합니다.");
        }
        if (request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
            throw new InvalidParameterException("durationSeconds는 0보다 커야 합니다.");
        }

        int paceSec = calcPaceSecondsPerKm(request.getDistanceMeters(), request.getDurationSeconds());
        int calories = (request.getCalories() != null && request.getCalories() >= 0)
                ? request.getCalories()
                : estimateCalories(request.getDistanceMeters());

        record.complete(
                metersToKm(request.getDistanceMeters()),   // km
                request.getDurationSeconds(),              // sec
                paceSec,                                   // sec/km
                calories,                                  // kcal
                LocalDateTime.now()
        );

        if (request.getRoutePoints() != null && !request.getRoutePoints().isEmpty()) {
            request.getRoutePoints().forEach(p ->
                    record.addRoutePoint(p.getLatitude(), p.getLongitude(), p.getSequence())
            );
        }

        // 사용자 통계 업데이트 (User 엔티티가 지원할 때)
        try {
            user.updateRunningStats(record.getDistance());
        } catch (Throwable ignore) {
            // 필드/메서드 미구현 환경에서도 동작하도록 무시
        }

        runningRecordRepository.save(record);
        userRepository.save(user);

        return RunningCompleteResponse.builder()
                .runningRecordId(record.getId())
                .totalDistanceMeters(request.getDistanceMeters())
                .durationSeconds(request.getDurationSeconds())
                .averagePaceSeconds(paceSec)
                .calories(calories)
                .endedAt(record.getEndedAt())
                .build();
    }

    // -------------------- utils --------------------
    private BigDecimal metersToKm(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    private int kmToMeters(BigDecimal km) {
        if (km == null) return 0;
        return km.multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int calcPaceSecondsPerKm(int distanceMeters, int durationSeconds) {
        if (distanceMeters <= 0 || durationSeconds <= 0) return 0;
        double km = distanceMeters / 1000.0;
        return (int) Math.round(durationSeconds / km);
    }

    private int estimateCalories(int distanceMeters) {
        double km = distanceMeters / 1000.0;
        return (int) Math.round(km * 60.0); // 러프: 60 kcal/km
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
