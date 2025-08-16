package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningUpdateRequest;
import com.waytoearth.dto.response.running.*;
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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final RunningRouteRepository runningRouteRepository;
    private final UserRepository userRepository; // ✅ 새로 주입해서 User를 DB에서 로드

    @Override
    public RunningStartResponse startRunning(AuthenticatedUser authUser, RunningStartRequest request) {
        // ✅ User는 new로 만들지 말고 DB에서 참조 가져오기 (protected 생성자/세터 없음)
        User runner = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RunningRecord record = new RunningRecord();
        record.setSessionId(java.util.UUID.randomUUID().toString());
        record.setUser(runner);
        record.setRunningType(request.getRunningType() != null ? request.getRunningType() : RunningType.SINGLE);
        record.setStatus(RunningStatus.RUNNING);
        record.setStartedAt(LocalDateTime.now());

        runningRecordRepository.save(record);
        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public RunningUpdateResponse updateRunning(AuthenticatedUser authUser, RunningUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 누적 통계 업데이트 (m → km)
        record.setDistance(BigDecimal.valueOf(request.getDistanceMeters() / 1000.0));
        record.setDuration(request.getDurationSeconds());
        record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        record.setCalories(request.getCalories());

        // 경로 1 포인트 추가
        if (request.getCurrentPoint() != null) {
            record.addRoutePoint(
                    request.getCurrentPoint().getLatitude(),
                    request.getCurrentPoint().getLongitude(),
                    request.getCurrentPoint().getSequence()
            );
        }

        return new RunningUpdateResponse(true);
    }

    @Override
    public RunningPauseResumeResponse pauseRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // ⚠️ pausedAt/pausedDurationSeconds 필드가 엔티티에 없으므로 상태만 변경
        record.setStatus(RunningStatus.PAUSED);

        return new RunningPauseResumeResponse(true, record.getStatus().name());
    }

    @Override
    public RunningPauseResumeResponse resumeRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // ⚠️ 시간 보정 필드가 없으므로 상태만 복구
        record.setStatus(RunningStatus.RUNNING);

        return new RunningPauseResumeResponse(true, record.getStatus().name());
    }

    @Override
    public RunningCompleteResponse completeRunning(AuthenticatedUser authUser, RunningCompleteRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        BigDecimal distanceKm = BigDecimal.valueOf(request.getDistanceMeters() / 1000.0);

        // 도메인 메서드 사용
        record.complete(
                distanceKm,
                request.getDurationSeconds(),
                request.getAveragePaceSeconds(),
                request.getCalories(),
                LocalDateTime.now()
        );

        // 전체 경로 저장
        if (request.getRoutePoints() != null) {
            request.getRoutePoints().forEach(p ->
                    record.addRoutePoint(p.getLatitude(), p.getLongitude(), p.getSequence())
            );
        }

        // 유저 통계 갱신 (User 엔티티의 도메인 메서드)
        User runner = record.getUser();
        if (runner != null) {
            runner.updateRunningStats(distanceKm);
        }

        // 응답 스펙에 정확히 맞춘 생성자 사용 (아래 2) 응답 DTO 교체본과 매칭됨)
        return new RunningCompleteResponse(
                record.getId(),
                distanceKm.doubleValue(),
                formatPace(record.getAveragePaceSeconds() != null ? record.getAveragePaceSeconds() : 0),
                record.getCalories(),
                request.getRoutePoints()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public java.util.List<RunningRecordSummaryResponse> getRecords(AuthenticatedUser authUser) {
        // 제목 필드가 엔티티에 없다면 title은 null로 내려보내거나 DTO에서 제외
        return runningRecordRepository
                .findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(authUser.getUserId())
                .stream()
                .map(r -> new RunningRecordSummaryResponse(
                        r.getId(),
                        null, // r.getTitle() 없다면 null
                        r.getDistance() != null ? r.getDistance().doubleValue() : 0.0,
                        r.getDuration() != null ? r.getDuration() : 0,
                        formatPace(r.getAveragePaceSeconds() != null ? r.getAveragePaceSeconds() : 0),
                        r.getCalories() != null ? r.getCalories() : 0,
                        r.getStartedAt() != null ? r.getStartedAt().toString() : null
                ))
                .toList();
    }

    private String formatPace(int paceSeconds) {
        int m = paceSeconds / 60;
        int s = paceSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}