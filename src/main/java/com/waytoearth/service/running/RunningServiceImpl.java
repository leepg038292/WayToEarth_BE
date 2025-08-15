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
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.RunningRecordRepository;
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

    @Override
    public RunningStartResponse startRunning(AuthenticatedUser user, RunningStartRequest request) {
        RunningRecord record = new RunningRecord();
        record.setSessionId(UUID.randomUUID().toString());
        record.setUser(user.getUser());
        record.setRunningType(request.getRunningType());
        record.setStartedAt(LocalDateTime.now());
        record.setStatus("RUNNING");
        runningRecordRepository.save(record);

        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public RunningUpdateResponse updateRunning(AuthenticatedUser user, RunningUpdateRequest request) {
        RunningRecord record = runningRecordRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 거리, 시간, 칼로리 업데이트
        record.setDistanceMeters(request.getDistanceMeters());
        record.setDurationSeconds(request.getDurationSeconds());
        record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        record.setCalories(request.getCalories());

        // 경로 저장
        RunningRoute route = new RunningRoute();
        route.setRunningRecord(record);
        route.setLatitude(request.getCurrentPoint().getLatitude());
        route.setLongitude(request.getCurrentPoint().getLongitude());
        route.setSequence(request.getCurrentPoint().getSequence());
        runningRouteRepository.save(route);

        return new RunningUpdateResponse(true);
    }

    @Override
    public RunningPauseResumeResponse pauseRunning(AuthenticatedUser user, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
        record.setStatus("PAUSED");
        record.setPausedAt(LocalDateTime.now());
        return new RunningPauseResumeResponse(true, "PAUSED");
    }

    @Override
    public RunningPauseResumeResponse resumeRunning(AuthenticatedUser user, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
        if (record.getPausedAt() != null) {
            long pausedSeconds = java.time.Duration.between(record.getPausedAt(), LocalDateTime.now()).getSeconds();
            record.setPausedDurationSeconds(record.getPausedDurationSeconds() + (int) pausedSeconds);
        }
        record.setStatus("RUNNING");
        record.setPausedAt(null);
        return new RunningPauseResumeResponse(true, "RUNNING");
    }

    @Override
    public RunningCompleteResponse completeRunning(AuthenticatedUser user, RunningCompleteRequest request) {
        RunningRecord record = runningRecordRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        record.setDistanceMeters(request.getDistanceMeters());
        record.setDurationSeconds(request.getDurationSeconds());
        record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        record.setCalories(request.getCalories());
        record.setStatus("COMPLETED");
        record.setEndedAt(LocalDateTime.now());

        // 전체 경로 저장
        request.getRoutePoints().forEach(point -> {
            RunningRoute route = new RunningRoute();
            route.setRunningRecord(record);
            route.setLatitude(point.getLatitude());
            route.setLongitude(point.getLongitude());
            route.setSequence(point.getSequence());
            runningRouteRepository.save(route);
        });

        return new RunningCompleteResponse(
                record.getId(),
                record.getDistanceMeters() / 1000.0,
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
