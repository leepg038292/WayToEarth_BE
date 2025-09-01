package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.VirtualCourseProgressUpdateRequest;
import com.waytoearth.dto.response.Virtual.SegmentProgressResponse;
import com.waytoearth.dto.response.Virtual.VirtualCourseProgressResponse;
import com.waytoearth.repository.VirtualRunning.SegmentProgressRepository;
import com.waytoearth.repository.VirtualRunning.UserVirtualCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserVirtualCourseServiceImpl implements UserVirtualCourseService {

    private final UserVirtualCourseRepository userVirtualCourseRepository;
    private final SegmentProgressRepository segmentProgressRepository;

    @Override
    public VirtualCourseProgressResponse updateProgress(Long userVirtualCourseId, VirtualCourseProgressUpdateRequest request) {
        var userCourse = userVirtualCourseRepository.findById(userVirtualCourseId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 코스를 찾을 수 없습니다."));

        // 세그먼트 진행률 업데이트
        var segmentProgress = segmentProgressRepository.findByUserVirtualCourseId(userVirtualCourseId).stream()
                .filter(sp -> sp.getSegmentId().equals(request.getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("세그먼트 진행 데이터를 찾을 수 없습니다."));

        segmentProgress.setDistanceAccumulated(
                segmentProgress.getDistanceAccumulated() + request.getDistanceKm()
        );
        segmentProgressRepository.save(segmentProgress);

        // 전체 코스 진행률 업데이트
        double newTotal = userCourse.getTotalDistanceAccumulated() + request.getDistanceKm();
        userCourse.setTotalDistanceAccumulated(newTotal);
        userCourse.setProgressPercent(calculateProgressPercent(userCourse.getCourseId(), newTotal));

        userVirtualCourseRepository.save(userCourse);

        return new VirtualCourseProgressResponse(
                userCourse.getProgressPercent(),
                userCourse.getTotalDistanceAccumulated(),
                userCourse.getStatus().name()
        );
    }

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

    private double calculateProgressPercent(Long courseId, double totalDistance) {
        // TODO: 실제 CourseRepository에서 총 거리 가져와 계산
        double courseTotal = 100.0; // 임시 값
        return Math.min(100.0, (totalDistance / courseTotal) * 100);
    }
}
