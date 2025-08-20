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
import com.waytoearth.service.emblem.EmblemService;
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
    private final EmblemService emblemService;  //엠블럼 자동 지급을 위한 의존성 주입


    @Override
    public RunningStartResponse startRunning(AuthenticatedUser authUser, RunningStartRequest request) {
        User runner = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ Builder 패턴으로 안전하게 생성
        RunningRecord record = RunningRecord.builder()
                .sessionId(request.getSessionId()) // ✅ 요청에서 받은 sessionId 사용
                .user(runner)
                .runningType(request.getRunningType() != null ? request.getRunningType() : RunningType.SINGLE)
                .virtualCourseId(request.getVirtualCourseId())
                .status(RunningStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .isCompleted(false) // ✅ 필수 필드 명시적 설정
                .build();

        runningRecordRepository.save(record);
        return new RunningStartResponse(record.getSessionId(), record.getStartedAt());
    }

    @Override
    public void updateRunning(AuthenticatedUser authUser, RunningUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 권한 검증 추가
        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
        }

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

        runningRecordRepository.save(record); // ✅ 저장 추가
    }

    @Override
    public void pauseRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 권한 검증 추가
        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
        }

        record.setStatus(RunningStatus.PAUSED);
        runningRecordRepository.save(record); // ✅ 저장 추가
    }

    @Override
    public void resumeRunning(AuthenticatedUser authUser, RunningPauseResumeRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId는 필수입니다.");
        }

        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
        }

        record.setStatus(RunningStatus.RUNNING);
        runningRecordRepository.save(record);
    }


    @Override
    public RunningCompleteResponse completeRunning(AuthenticatedUser authUser, RunningCompleteRequest request) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 권한 검증 추가
        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
        }

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

        // 사용자 통계 업데이트
        User user = record.getUser();
        user.updateRunningStats(distanceKm);
        userRepository.save(user);

        // 저장 후 응답 구성
        runningRecordRepository.save(record);

        // 엠블럼 자동 지급
        var awardResult = emblemService.scanAndAward(user.getId(), "DISTANCE");

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
                        .collect(Collectors.toList()),
                awardResult
        );

    }

    @Override
    public void updateTitle(AuthenticatedUser authUser, Long recordId, RunningTitleUpdateRequest request) {
        RunningRecord record = runningRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        // 소유권 검증
        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 기록에 대한 권한이 없습니다.");
        }

        record.setTitle(request.getTitle());
        runningRecordRepository.save(record); // ✅ 저장 추가
    }

    @Override
    @Transactional(readOnly = true)
    public RunningCompleteResponse getDetail(AuthenticatedUser authUser, Long recordId) {
        RunningRecord r = runningRecordRepository.findWithRoutesById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        // 소유권 검증
        if (!r.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 기록에 대한 권한이 없습니다.");
        }

        return RunningCompleteResponse.builder()
                .runningRecordId(r.getId())
                .title(r.getTitle())
                .totalDistanceKm(r.getDistance() != null ? r.getDistance().doubleValue() : 0.0)
                .averagePace(formatPace(r.getAveragePaceSeconds()))
                .calories(r.getCalories())
                .startedAt(r.getStartedAt() != null ? r.getStartedAt().toString() : null)
                .endedAt(r.getEndedAt() != null ? r.getEndedAt().toString() : null)
                .routePoints(
                        r.getRoutes().stream()
                                .map(rt -> new RunningCompleteResponse.RoutePoint(
                                        rt.getLatitude(), rt.getLongitude(), rt.getSequence()
                                ))
                                .collect(Collectors.toList())
                )
                .emblemAwardResult(null) // 상세조회에서는 지급 결과 없음
                .build();
    }

    private String formatPace(Integer paceSeconds) {
        if (paceSeconds == null) return "00:00";
        int m = paceSeconds / 60;
        int s = paceSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}