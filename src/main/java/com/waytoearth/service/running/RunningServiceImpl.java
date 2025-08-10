package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.service.running.RunningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

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
                .build();

        runningRecordRepository.save(record);

        return RunningStartResponse.builder()
                .sessionId(record.getSessionId())
                .startedAt(record.getStartedAt())
                .build();
    }

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

        // 이미 완료된 세션이면 그대로 반환
        if (Boolean.TRUE.equals(record.getIsCompleted())) {
            return RunningCompleteResponse.builder()
                    .runningRecordId(record.getId())
                    .totalDistanceMeters(kmToMeters(record.getDistance()))
                    .durationSeconds(record.getDuration())
                    .averagePaceSeconds(record.getAveragePaceSeconds() == null ? 0 : record.getAveragePaceSeconds())
                    .calories(record.getCalories() == null ? 0 : record.getCalories())
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

        // DB 저장 (km/초/초단위페이스/칼로리)
        record.complete(
                metersToKm(request.getDistanceMeters()),
                request.getDurationSeconds(),
                paceSec,
                calories,
                LocalDateTime.now()
        );

        if (request.getRoutePoints() != null && !request.getRoutePoints().isEmpty()) {
            request.getRoutePoints().forEach(p ->
                    record.addRoutePoint(p.getLatitude(), p.getLongitude(), p.getSequence())
            );
        }

        // 사용자 통계 (엔티티에 맞춰 km 단위로 합산)
        user.updateRunningStats(record.getDistance());

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
        // 러프값: 60 kcal/km
        double km = distanceMeters / 1000.0;
        return (int) Math.round(km * 60.0);
    }
}