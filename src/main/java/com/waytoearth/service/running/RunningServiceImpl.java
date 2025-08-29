package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.*;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningRecordSummaryResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.RunningRouteRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.emblem.EmblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

        //  Builder 패턴으로 안전하게 생성
        RunningRecord record = RunningRecord.builder()
                .sessionId(request.getSessionId()) //  요청에서 받은 sessionId 사용
                .user(runner)
                .runningType(request.getRunningType() != null ? request.getRunningType() : RunningType.SINGLE)
                .virtualCourseId(request.getVirtualCourseId())
                .status(RunningStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .isCompleted(false) //  필수 필드 명시적 설정
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

        runningRecordRepository.save(record); //  저장 추가
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
        runningRecordRepository.save(record); //  저장 추가
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
        System.out.println("=== completeRunning 시작 ===");
        System.out.println("SessionId: " + request.getSessionId());
        System.out.println("UserId: " + authUser.getUserId());

        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        System.out.println("찾은 기록 ID: " + record.getId());
        System.out.println("기록 소유자 ID: " + record.getUser().getId());

        // 권한 검증
        if (!record.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
        }

        BigDecimal distanceKm = BigDecimal.valueOf(request.getDistanceMeters() / 1000.0);
        System.out.println("거리(km): " + distanceKm);

        // 완료 처리 전 상태
        System.out.println("완료 처리 전 - isCompleted: " + record.getIsCompleted());
        System.out.println("완료 처리 전 - status: " + record.getStatus());

        // 도메인 메서드 사용
        record.complete(
                distanceKm,
                request.getDurationSeconds(),
                request.getAveragePaceSeconds(),
                request.getCalories(),
                LocalDateTime.now()
        );

        // 완료 처리 후 상태
        System.out.println("완료 처리 후 - isCompleted: " + record.getIsCompleted());
        System.out.println("완료 처리 후 - status: " + record.getStatus());

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
        RunningRecord savedRecord = runningRecordRepository.save(record);
        System.out.println("저장된 기록 ID: " + savedRecord.getId());
        System.out.println("저장된 기록 완료상태: " + savedRecord.getIsCompleted());

        // ✅ 강제 flush
        runningRecordRepository.flush();
        System.out.println("flush 완료");

        // 엠블럼 자동 지급
        var awardResult = emblemService.scanAndAward(user.getId(), "DISTANCE");

        System.out.println("=== completeRunning 완료 ===");

        return new RunningCompleteResponse(
                savedRecord.getId(),
                savedRecord.getTitle(),
                savedRecord.getDistance() != null ? savedRecord.getDistance().doubleValue() : 0.0,
                formatPace(savedRecord.getAveragePaceSeconds()),
                savedRecord.getCalories(),
                savedRecord.getStartedAt() != null ? savedRecord.getStartedAt().toString() : null,
                savedRecord.getEndedAt() != null ? savedRecord.getEndedAt().toString() : null,
                savedRecord.getRoutes().stream()
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
        runningRecordRepository.save(record); //  저장 추가
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

    @Transactional(readOnly = true)
    @Override
    public Page<RunningRecordSummaryResponse> getRecords(AuthenticatedUser authUser, Pageable pageable) {
        System.out.println("=== getRecords 시작 ===");
        System.out.println("요청 사용자 ID: " + authUser.getUserId());

        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        System.out.println("찾은 사용자: " + user.getId());

        // ✅ 디버깅: 해당 사용자의 모든 기록 확인
        List<RunningRecord> allRecords = runningRecordRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());

        System.out.println("해당 사용자의 전체 기록 수: " + allRecords.size());
        allRecords.forEach(r -> {
            System.out.println("- 기록 ID: " + r.getId() +
                    ", SessionId: " + r.getSessionId() +
                    ", 완료여부: " + r.getIsCompleted() +
                    ", 상태: " + r.getStatus() +
                    ", 거리: " + r.getDistance());
        });

        // ✅ 완료된 기록만 필터링해서 확인
        long completedCount = allRecords.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCompleted()))
                .count();

        System.out.println("완료된 기록 수: " + completedCount);

        Page<RunningRecord> pageResult = runningRecordRepository
                .findByUserAndIsCompletedTrueOrderByStartedAtDesc(user, pageable);

        System.out.println("페이지 결과 - 총 요소: " + pageResult.getTotalElements());
        System.out.println("페이지 결과 - 현재 페이지 크기: " + pageResult.getContent().size());

        Page<RunningRecordSummaryResponse> result = pageResult.map(r -> {
            System.out.println("매핑 중인 기록: " + r.getId());
            return new RunningRecordSummaryResponse(
                    r.getId(),
                    r.getTitle(),
                    r.getDistance() != null ? r.getDistance().doubleValue() : 0.0,
                    r.getDuration() != null ? r.getDuration() : 0,
                    formatPace(r.getAveragePaceSeconds()),
                    r.getCalories() != null ? r.getCalories() : 0,
                    r.getStartedAt() != null ? r.getStartedAt().toString() : null
            );
        });

        System.out.println("=== getRecords 완료 ===");
        return result;
    }

}