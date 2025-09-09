package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.UserVirtualCourseCreateRequest;
import com.waytoearth.dto.request.Virtual.VirtualCourseProgressUpdateRequest;
import com.waytoearth.dto.response.Virtual.SegmentProgressResponse;
import com.waytoearth.dto.response.Virtual.UserVirtualCourseResponse;
import com.waytoearth.dto.response.Virtual.VirtualCourseProgressResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.VirtualRunning.CourseSegmentEntity;
import com.waytoearth.entity.VirtualRunning.ProgressUpdateLog;
import com.waytoearth.entity.VirtualRunning.SegmentProgressEntity;
import com.waytoearth.entity.VirtualRunning.UserVirtualCourseEntity;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.entity.enums.VirtualCourseStatus;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.repository.VirtualRunning.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserVirtualCourseServiceImpl implements UserVirtualCourseService {

    private final UserVirtualCourseRepository userVirtualCourseRepository;
    private final SegmentProgressRepository segmentProgressRepository;
    private final ThemeCourseRepository themeCourseRepository;
    private final CustomCourseRepository customCourseRepository;
    private final CourseSegmentRepository courseSegmentRepository;
    private final ProgressUpdateLogRepository progressUpdateLogRepository;

    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    /**
     * ✅ 진행률 업데이트 (동시성 안전 버전)
     */
    @Override
    public VirtualCourseProgressResponse updateProgress(Long userVirtualCourseId, VirtualCourseProgressUpdateRequest request) {
        log.debug("진행률 업데이트 시작 - userVirtualCourseId: {}, segmentId: {}, distance: {}", 
                  userVirtualCourseId, request.getSegmentId(), request.getDistanceKm());

        // 1. 입력값 검증
        validateProgressRequest(request);

        // 2. 중복 요청 체크
        if (isDuplicateRequest(request)) {
            log.warn("중복 요청 감지 - sessionId: {}, segmentId: {}, distance: {}", 
                     request.getSessionId(), request.getSegmentId(), request.getDistanceKm());
            throw new IllegalStateException("30초 내 동일한 업데이트 요청이 있습니다.");
        }

        try {
            // 3. 중복 방지 로그 저장
            saveProgressUpdateLog(request);

            // 4. 원자적 세그먼트 거리 업데이트
            int segmentUpdated = segmentProgressRepository.addDistanceAtomically(
                userVirtualCourseId, request.getSegmentId(), request.getDistanceKm());
            
            if (segmentUpdated == 0) {
                throw new IllegalArgumentException("세그먼트 진행 데이터를 찾을 수 없습니다.");
            }

            // 5. 원자적 총 거리 업데이트
            int totalUpdated = userVirtualCourseRepository.addTotalDistanceAtomically(
                userVirtualCourseId, request.getDistanceKm());
            
            if (totalUpdated == 0) {
                throw new IllegalArgumentException("사용자 코스 데이터를 찾을 수 없습니다.");
            }

            // 6. 세그먼트 완료 상태 확인 및 업데이트
            segmentProgressRepository.updateSegmentStatusIfCompleted(
                userVirtualCourseId, request.getSegmentId(), VirtualCourseStatus.COMPLETED);

            // 7. 현재 상태 조회 (업데이트된 값으로)
            var userCourse = userVirtualCourseRepository.findById(userVirtualCourseId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 코스를 찾을 수 없습니다."));

            // 8. 진행률 계산 및 업데이트
            double courseTotal = getCourseTotalDistance(userCourse);
            double progressPercent = Math.min(100.0, (userCourse.getTotalDistanceAccumulated() / courseTotal) * 100);
            
            VirtualCourseStatus newStatus = userCourse.getStatus();
            boolean isCourseCompleted = userCourse.getTotalDistanceAccumulated() >= courseTotal;
            
            if (isCourseCompleted && userCourse.getStatus() != VirtualCourseStatus.COMPLETED) {
                newStatus = VirtualCourseStatus.COMPLETED;
            }

            // 9. 진행률 및 상태 업데이트
            userVirtualCourseRepository.updateProgressAndStatus(userVirtualCourseId, progressPercent, newStatus);

            // 10. RunningRecord 동기화
            syncRunningRecord(request, userCourse.getTotalDistanceAccumulated(), isCourseCompleted, courseTotal);

            log.debug("진행률 업데이트 완료 - 총 거리: {}, 진행률: {}%", 
                      userCourse.getTotalDistanceAccumulated(), progressPercent);

            return new VirtualCourseProgressResponse(
                progressPercent,
                userCourse.getTotalDistanceAccumulated(),
                newStatus.name()
            );

        } catch (Exception e) {
            log.error("진행률 업데이트 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * ✅ 전체 진행률 조회
     */
    @Override
    public VirtualCourseProgressResponse getProgress(Long userVirtualCourseId) {
        var userCourse = userVirtualCourseRepository.findById(userVirtualCourseId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 코스를 찾을 수 없습니다."));

        return new VirtualCourseProgressResponse(
                userCourse.getProgressPercent(),
                userCourse.getTotalDistanceAccumulated(),
                userCourse.getStatus().name()
        );
    }

    /**
     * ✅ 세그먼트별 진행률 조회
     */
    @Override
    public List<SegmentProgressResponse> getSegmentProgress(Long userVirtualCourseId) {
        return segmentProgressRepository.findByUserVirtualCourseId(userVirtualCourseId)
                .stream()
                .map(sp -> new SegmentProgressResponse(
                        sp.getSegmentId(),
                        sp.getDistanceAccumulated(),
                        sp.getStatus().name()
                ))
                .toList();
    }

    /**
     * ✅ 사용자 가상 코스 생성 + 세그먼트 진행률 초기화 + RunningRecord 생성
     */
    @Override
    public UserVirtualCourseResponse createUserVirtualCourse(UserVirtualCourseCreateRequest request) {
        // 1. UserVirtualCourseEntity 저장
        var userCourse = UserVirtualCourseEntity.builder()
                .userId(request.userId())
                .courseId(request.courseId())
                .courseType(UserVirtualCourseEntity.CourseType.valueOf(request.courseType()))
                .totalDistanceAccumulated(0.0)
                .progressPercent(0.0)
                .status(VirtualCourseStatus.ACTIVE)
                .build();

        var savedCourse = userVirtualCourseRepository.save(userCourse);

        // 2. 해당 코스의 세그먼트 목록 가져오기
        List<CourseSegmentEntity> segments;
        if (savedCourse.getCourseType() == UserVirtualCourseEntity.CourseType.CUSTOM) {
            segments = courseSegmentRepository.findByCustomCourseIdOrderByOrderIndex(savedCourse.getCourseId());
        } else {
            segments = courseSegmentRepository.findByThemeCourseIdOrderByOrderIndex(savedCourse.getCourseId());
        }

        // 3. 세그먼트별 SegmentProgressEntity 초기화
        List<SegmentProgressEntity> progressList = segments.stream()
                .map(seg -> SegmentProgressEntity.builder()
                        .userVirtualCourse(savedCourse)
                        .segmentId(seg.getId())
                        .distanceAccumulated(0.0)
                        .status(VirtualCourseStatus.ACTIVE)
                        .build()
                )
                .toList();

        segmentProgressRepository.saveAll(progressList);

        // 4. RunningRecord 생성 (sessionId 포함)
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String sessionId = UUID.randomUUID().toString();

        RunningRecord record = RunningRecord.builder()
                .sessionId(sessionId)
                .user(user)
                .runningType(RunningType.VIRTUAL)
                .virtualCourseId(savedCourse.getId())
                .status(RunningStatus.RUNNING)
                .isCompleted(false)
                .startedAt(LocalDateTime.now())
                .build();

        runningRecordRepository.save(record);

        // 5. 응답 반환 (sessionId 포함)
        return new UserVirtualCourseResponse(
                savedCourse.getId(),
                savedCourse.getUserId(),
                savedCourse.getCourseId(),
                savedCourse.getCourseType().name(),
                savedCourse.getTotalDistanceAccumulated(),
                savedCourse.getProgressPercent(),
                savedCourse.getStatus().name(),
                sessionId
        );
    }

    /**
     * ✅ 입력값 검증
     */
    private void validateProgressRequest(VirtualCourseProgressUpdateRequest request) {
        if (request.getDistanceKm() == null || request.getDistanceKm() <= 0) {
            throw new IllegalArgumentException("거리는 0보다 커야 합니다.");
        }
        if (request.getDistanceKm() > 50) {
            throw new IllegalArgumentException("한 번에 50km 이상 진행할 수 없습니다.");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }
        if (request.getSegmentId() == null) {
            throw new IllegalArgumentException("세그먼트 ID는 필수입니다.");
        }
    }

    /**
     * ✅ 중복 요청 체크
     */
    private boolean isDuplicateRequest(VirtualCourseProgressUpdateRequest request) {
        return progressUpdateLogRepository.existsDuplicateRequest(
            request.getSessionId(),
            request.getSegmentId(),
            request.getDistanceKm(),
            LocalDateTime.now().minusSeconds(30)
        );
    }

    /**
     * ✅ 진행률 업데이트 로그 저장
     */
    private void saveProgressUpdateLog(VirtualCourseProgressUpdateRequest request) {
        String logId = ProgressUpdateLog.generateId(
            request.getSessionId(), 
            request.getSegmentId(), 
            request.getDistanceKm()
        );
        
        ProgressUpdateLog log = ProgressUpdateLog.builder()
            .id(logId)
            .sessionId(request.getSessionId())
            .segmentId(request.getSegmentId())
            .distanceKm(request.getDistanceKm())
            .build();
        
        progressUpdateLogRepository.save(log);
    }

    /**
     * ✅ RunningRecord 동기화
     */
    private void syncRunningRecord(VirtualCourseProgressUpdateRequest request, 
                                  double totalDistance, 
                                  boolean isCourseCompleted,
                                  double courseTotal) {
        RunningRecord record = runningRecordRepository.findBySessionId(request.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // 기본 정보 업데이트
        record.setDistance(BigDecimal.valueOf(totalDistance));
        if (request.getDurationSeconds() != null) {
            record.setDuration(request.getDurationSeconds());
        }
        if (request.getCalories() != null) {
            record.setCalories(request.getCalories());
        }
        if (request.getAveragePaceSeconds() != null) {
            record.setAveragePaceSeconds(request.getAveragePaceSeconds());
        }

        // 경로 데이터 추가
        if (request.getCurrentPoint() != null) {
            record.addRoutePoint(
                request.getCurrentPoint().getLatitude(),
                request.getCurrentPoint().getLongitude(),
                request.getCurrentPoint().getSequence()
            );
        }

        // 코스 완료 처리
        if (isCourseCompleted && !record.getIsCompleted()) {
            record.complete(
                BigDecimal.valueOf(courseTotal),
                request.getDurationSeconds(),
                request.getAveragePaceSeconds(),
                request.getCalories(),
                LocalDateTime.now()
            );

            // 사용자 통계 업데이트
            User user = record.getUser();
            user.updateRunningStats(BigDecimal.valueOf(courseTotal));
            userRepository.save(user);
        }

        runningRecordRepository.save(record);
    }

    /**
     * ✅ 코스 총 거리 조회
     */
    private double getCourseTotalDistance(UserVirtualCourseEntity userCourse) {
        return switch (userCourse.getCourseType()) {
            case THEME -> themeCourseRepository.findById(userCourse.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("테마 코스를 찾을 수 없습니다."))
                    .getTotalDistanceKm();
            case CUSTOM -> customCourseRepository.findById(userCourse.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("커스텀 코스를 찾을 수 없습니다."))
                    .getTotalDistanceKm();
        };
    }
}
