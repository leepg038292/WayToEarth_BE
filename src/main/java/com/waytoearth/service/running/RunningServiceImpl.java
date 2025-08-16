package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningUpdateRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningPauseResumeResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.dto.response.running.RunningUpdateResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.RunningRoute;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.RunningRouteRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final RunningRouteRepository runningRouteRepository;
    private final UserRepository userRepository;

    @Override
    public RunningStartResponse startRunning(AuthenticatedUser user, RunningStartRequest request) {
        User runner = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RunningRecord record = new RunningRecord();
        record.setSessionId(UUID.randomUUID().toString());
        record.setUser(runner);
        record.setRunningType(RunningType.valueOf(request.getRunningType().name()));
        record.setStartedAt(LocalDateTime.now());
        record.setStatus(RunningStatus.RUNNING);
        runningRecordRepository.save(record);

        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public RunningUpdateResponse updateRunning(AuthenticatedUser user, RunningUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 거리, 시간, 페이스, 칼로리 업데이트
        record.setDistance(BigDecimal.valueOf(request.getDistanceMeters() / 1000.0));
        record.setDuration(request.getDurationSeconds());
        record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        record.setCalories(request.getCalories());

        // 경로 추가
        record.addRoutePoint(
                request.getCurrentPoint().getLatitude(),
                request.getCurrentPoint().getLongitude(),
                request.getCurrentPoint().getSequence()
        );

        return new RunningUpdateResponse(true);
    }

    @Override
    public RunningPauseResumeResponse pauseRunning(AuthenticatedUser user, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        record.setStatus(RunningStatus.PAUSED);
        record.setPausedAt(LocalDateTime.now());

        return new RunningPauseResumeResponse(true, record.getStatus().name());
    }

    @Override
    public RunningPauseResumeResponse resumeRunning(AuthenticatedUser user, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        if (record.getPausedAt() != null) {
            long pausedSeconds = java.time.Duration.between(record.getPausedAt(), LocalDateTime.now()).getSeconds();
            record.setPausedDurationSeconds(record.getPausedDurationSeconds() + (int) pausedSeconds);
        }
        record.setStatus(RunningStatus.RUNNING);
        record.setPausedAt(null);

        return new RunningPauseResumeResponse(true, record.getStatus().name());
    }

    @Override
    public RunningCompleteResponse completeRunning(AuthenticatedUser user, RunningCompleteRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 최종 기록 반영 (도메인 메서드 사용)
        record.complete(
                BigDecimal.valueOf(request.getDistanceMeters() / 1000.0),
                request.getDurationSeconds(),
                request.getAveragePaceSeconds(),
                request.getCalories(),
                LocalDateTime.now()
        );

        // 경로 전체 저장
        request.getRoutePoints().forEach(point -> {
            record.addRoutePoint(point.getLatitude(), point.getLongitude(), point.getSequence());
        });

        // 유저 통계 업데이트
        User runner = record.getUser();
        runner.updateRunningStats(record.getDistance());

        return new RunningCompleteResponse(
                record.getId(),
                record.getDistance().doubleValue(),
                formatPace(record.getAveragePaceSeconds()),
                record.getCalories(),
                request.getRoutePoints()
        );
    }

    private String formatPace(int paceSeconds) {
        int minutes = paceSeconds / 60;
        int seconds = paceSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}