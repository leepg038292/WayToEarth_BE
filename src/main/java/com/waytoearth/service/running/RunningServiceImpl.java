package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.*;
import com.waytoearth.dto.response.running.*;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.RunningRouteRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RunningServiceImpl implements RunningService {

    private final RunningRecordRepository runningRecordRepository;
    private final RunningRouteRepository runningRouteRepository;
    private final UserRepository userRepository;

    @Override
    public RunningStartResponse startRunning(AuthenticatedUser authUser, RunningStartRequest request) {
        User runner = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RunningRecord record = new RunningRecord();
        record.setSessionId(java.util.UUID.randomUUID().toString());
        record.setUser(runner);
        // 요청에 없으면 기본 SINGLE
        RunningType type = request.getRunningType() != null ? request.getRunningType() : RunningType.SINGLE;
        record.setRunningType(type);
        record.setStatus(RunningStatus.RUNNING);
        record.setStartedAt(LocalDateTime.now());

        runningRecordRepository.save(record);
        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public void updateRunning(AuthenticatedUser authUser, RunningUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 누적값 반영 (m → km)
        record.setDistance(BigDecimal.valueOf(request.getDistanceMeters() / 1000.0));
        record.setDuration(request.getDurationSeconds());
        record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        record.setCalories(request.getCalories());

        if (request.getCurrentPoint() != null) {
            record.addRoutePoint(
                    request.getCurrentPoint().getLatitude(),
                    request.getCurrentPoint().getLongitude(),
                    request.getCurrentPoint().getSequence()
            );
        }
    }

    @Override
    public void pauseRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
        // 엔티티에 일시정지 시간/누적필드가 없으므로 상태만 전환
        record.setStatus(RunningStatus.PAUSED);
    }

    @Override
    public void resumeRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
        // 엔티티에 보정 필드가 없으므로 상태만 전환
        record.setStatus(RunningStatus.RUNNING);
    }

    @Override
    public RunningCompleteResponse completeRunning(AuthenticatedUser authUser, RunningCompleteRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        BigDecimal distanceKm = BigDecimal.valueOf(request.getDistanceMeters() / 1000.0);

        // 도메인 메서드 사용: complete(...)
        record.complete(
                distanceKm,
                request.getDurationSeconds(),
                request.getAveragePaceSeconds(),
                request.getCalories(),
                LocalDateTime.now()
        );

        // 경로 전체 추가
        if (request.getRoutePoints() != null) {
            request.getRoutePoints().forEach(p ->
                    record.addRoutePoint(p.getLatitude(), p.getLongitude(), p.getSequence())
            );
        }

        // 저장 후 응답 구성
        runningRecordRepository.save(record);

        return new RunningCompleteResponse(
                record.getId(),
                record.getTitle(), // 제목은 PATCH로 수정 가능
                record.getDistance() != null ? record.getDistance().doubleValue() : 0.0,
                formatPace(record.getAveragePaceSeconds()),
                record.getCalories(),
                record.getStartedAt() != null ? record.getStartedAt().toString() : null,
                record.getEndedAt() != null ? record.getEndedAt().toString() : null,
                record.getRoutes().stream()
                        .map(rt -> new RunningCompleteResponse.RoutePoint(
                                rt.getLatitude(), rt.getLongitude(), rt.getSequence()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void updateTitle(AuthenticatedUser authUser, Long recordId, RunningTitleUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));
        // (선택) 소유권 검증: authUser.getUserId() vs record.getUser().getId()
        record.setTitle(request.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public RunningCompleteResponse getDetail(AuthenticatedUser authUser, Long recordId) {
        RunningRecord r = runningRecordRepository.findWithRoutesById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        return new RunningCompleteResponse(
                r.getId(),
                r.getTitle(),
                r.getDistance() != null ? r.getDistance().doubleValue() : 0.0,
                formatPace(r.getAveragePaceSeconds()),
                r.getCalories(),
                r.getStartedAt() != null ? r.getStartedAt().toString() : null,
                r.getEndedAt() != null ? r.getEndedAt().toString() : null,
                r.getRoutes().stream()
                        .map(rt -> new RunningCompleteResponse.RoutePoint(
                                rt.getLatitude(), rt.getLongitude(), rt.getSequence()
                        ))
                        .collect(Collectors.toList())
        );
    }

    private String formatPace(Integer paceSeconds) {
        if (paceSeconds == null) return "00:00";
        int m = paceSeconds / 60;
        int s = paceSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}