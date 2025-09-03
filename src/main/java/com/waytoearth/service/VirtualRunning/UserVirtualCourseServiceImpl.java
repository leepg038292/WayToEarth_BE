package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.UserVirtualCourseCreateRequest;
import com.waytoearth.dto.request.Virtual.VirtualCourseProgressUpdateRequest;
import com.waytoearth.dto.response.Virtual.SegmentProgressResponse;
import com.waytoearth.dto.response.Virtual.UserVirtualCourseResponse;
import com.waytoearth.dto.response.Virtual.VirtualCourseProgressResponse;
import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.VirtualRunning.UserVirtualCourseEntity;
import com.waytoearth.entity.VirtualRunning.CourseSegmentEntity;
import com.waytoearth.entity.VirtualRunning.SegmentProgressEntity;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.entity.enums.VirtualCourseStatus;
import com.waytoearth.repository.RunningRecordRepository;
import com.waytoearth.repository.UserRepository;
import com.waytoearth.repository.VirtualRunning.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserVirtualCourseServiceImpl implements UserVirtualCourseService {

    private final UserVirtualCourseRepository userVirtualCourseRepository;
    private final SegmentProgressRepository segmentProgressRepository;
    private final ThemeCourseRepository themeCourseRepository;
    private final CustomCourseRepository customCourseRepository;
    private final CourseSegmentRepository courseSegmentRepository;

    // ✅ 추가
    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    /**
     * ✅ 진행률 업데이트
     */
    @Override
    public VirtualCourseProgressResponse updateProgress(Long userVirtualCourseId, VirtualCourseProgressUpdateRequest request) {
        var userCourse = userVirtualCourseRepository.findById(userVirtualCourseId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 코스를 찾을 수 없습니다."));

        // 세그먼트 진행률 업데이트
        var segmentProgress = segmentProgressRepository.findByUserVirtualCourseId(userVirtualCourseId).stream()
                .filter(sp -> sp.getSegmentId().equals(request.getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("세그먼트 진행 데이터를 찾을 수 없습니다."));

        double updatedDistance = segmentProgress.getDistanceAccumulated() + request.getDistanceKm();
        segmentProgress.setDistanceAccumulated(updatedDistance);

        // 세그먼트 완료 여부 체크
        var segmentEntity = courseSegmentRepository.findById(request.getSegmentId())
                .orElseThrow(() -> new IllegalArgumentException("세그먼트를 찾을 수 없습니다."));
        if (updatedDistance >= segmentEntity.getDistanceKm()) {
            segmentProgress.setStatus(VirtualCourseStatus.COMPLETED);
        }
        segmentProgressRepository.save(segmentProgress);

        // 전체 코스 진행률 업데이트
        double newTotal = userCourse.getTotalDistanceAccumulated() + request.getDistanceKm();
        userCourse.setTotalDistanceAccumulated(newTotal);
        userCourse.setProgressPercent(calculateProgressPercent(userCourse, newTotal));

        // 코스 완료 체크
        double courseTotal = getCourseTotalDistance(userCourse);
        if (newTotal >= courseTotal) {
            userCourse.setStatus(VirtualCourseStatus.COMPLETED);

            // ✅ RunningRecord 저장
            User user = userRepository.findById(userCourse.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            RunningRecord record = RunningRecord.builder()
                    .user(user)
                    .runningType(RunningType.VIRTUAL)
                    .virtualCourseId(userCourse.getId())
                    .distance(BigDecimal.valueOf(courseTotal))
                    .status(RunningStatus.COMPLETED)
                    .isCompleted(true)
                    .startedAt(LocalDateTime.now().minusDays(1)) // TODO: 실제 시작 시간 연동 필요
                    .endedAt(LocalDateTime.now())
                    .build();

            runningRecordRepository.save(record);

            // ✅ 유저 통계 업데이트
            user.updateRunningStats(BigDecimal.valueOf(courseTotal));
            userRepository.save(user);
        }

        userVirtualCourseRepository.save(userCourse);

        return new VirtualCourseProgressResponse(
                userCourse.getProgressPercent(),
                userCourse.getTotalDistanceAccumulated(),
                userCourse.getStatus().name()
        );
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
     * ✅ 사용자 가상 코스 생성 + 세그먼트 진행률 초기화
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

        // 4. 응답 반환
        return new UserVirtualCourseResponse(
                savedCourse.getId(),
                savedCourse.getUserId(),
                savedCourse.getCourseId(),
                savedCourse.getCourseType().name(),
                savedCourse.getTotalDistanceAccumulated(),
                savedCourse.getProgressPercent(),
                savedCourse.getStatus().name()
        );
    }

    /**
     * ✅ 진행률 계산
     */
    private double calculateProgressPercent(UserVirtualCourseEntity userCourse, double totalDistance) {
        double courseTotal = getCourseTotalDistance(userCourse);
        return Math.min(100.0, (totalDistance / courseTotal) * 100);
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
