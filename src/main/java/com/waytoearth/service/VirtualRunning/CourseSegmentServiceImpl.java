package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;
import com.waytoearth.entity.VirtualRunning.CourseSegmentEntity;
import com.waytoearth.repository.VirtualRunning.CourseSegmentRepository;
import com.waytoearth.repository.VirtualRunning.ThemeCourseRepository;
import com.waytoearth.repository.VirtualRunning.CustomCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseSegmentServiceImpl implements CourseSegmentService {

    private final CourseSegmentRepository courseSegmentRepository;
    private final ThemeCourseRepository themeCourseRepository;
    private final CustomCourseRepository customCourseRepository;

    // ✅ 테마 코스에 세그먼트 추가
    @Override
    public CourseSegmentDetailResponse addSegmentToThemeCourse(Long themeCourseId, CourseSegmentCreateRequest request) {
        var themeCourse = themeCourseRepository.findById(themeCourseId)
                .orElseThrow(() -> new IllegalArgumentException("테마 코스를 찾을 수 없습니다. id=" + themeCourseId));

        var segment = CourseSegmentEntity.builder()
                .themeCourse(themeCourse)
                .type(request.getType())
                .orderIndex(request.getOrderIndex())
                .startLat(request.getStartLat())
                .startLng(request.getStartLng())
                .endLat(request.getEndLat())
                .endLng(request.getEndLng())
                .distanceKm(request.getDistanceKm())
                .build();

        var saved = courseSegmentRepository.save(segment);

        return new CourseSegmentDetailResponse(
                saved.getId(),
                saved.getType().name(),
                saved.getStartLat(),
                saved.getStartLng(),
                saved.getEndLat(),
                saved.getEndLng(),
                saved.getDistanceKm()
        );
    }

    // ✅ 커스텀 코스에 세그먼트 추가
    @Override
    public CourseSegmentDetailResponse addSegmentToCustomCourse(Long customCourseId, CourseSegmentCreateRequest request) {
        var customCourse = customCourseRepository.findById(customCourseId)
                .orElseThrow(() -> new IllegalArgumentException("커스텀 코스를 찾을 수 없습니다. id=" + customCourseId));

        var segment = CourseSegmentEntity.builder()
                .customCourse(customCourse)
                .type(request.getType())
                .orderIndex(request.getOrderIndex())
                .startLat(request.getStartLat())
                .startLng(request.getStartLng())
                .endLat(request.getEndLat())
                .endLng(request.getEndLng())
                .distanceKm(request.getDistanceKm())
                .build();

        var saved = courseSegmentRepository.save(segment);

        return new CourseSegmentDetailResponse(
                saved.getId(),
                saved.getType().name(),
                saved.getStartLat(),
                saved.getStartLng(),
                saved.getEndLat(),
                saved.getEndLng(),
                saved.getDistanceKm()
        );
    }

    // ✅ 테마 코스 세그먼트 목록
    @Override
    public List<CourseSegmentSummaryResponse> getSegmentsByThemeCourse(Long themeCourseId) {
        return courseSegmentRepository.findByThemeCourseIdOrderByOrderIndex(themeCourseId)
                .stream()
                .map(seg -> new CourseSegmentSummaryResponse(
                        seg.getId(),
                        seg.getOrderIndex(),
                        seg.getDistanceKm()
                ))
                .toList();
    }

    // ✅ 커스텀 코스 세그먼트 목록
    @Override
    public List<CourseSegmentSummaryResponse> getSegmentsByCustomCourse(Long customCourseId) {
        return courseSegmentRepository.findByCustomCourseIdOrderByOrderIndex(customCourseId)
                .stream()
                .map(seg -> new CourseSegmentSummaryResponse(
                        seg.getId(),
                        seg.getOrderIndex(),
                        seg.getDistanceKm()
                ))
                .toList();
    }

    // ✅ 세그먼트 상세 조회
    @Override
    public CourseSegmentDetailResponse getSegmentDetail(Long segmentId) {
        var seg = courseSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("세그먼트를 찾을 수 없습니다. id=" + segmentId));

        return new CourseSegmentDetailResponse(
                seg.getId(),
                seg.getType().name(),
                seg.getStartLat(),
                seg.getStartLng(),
                seg.getEndLat(),
                seg.getEndLng(),
                seg.getDistanceKm()
        );
    }

    // ✅ 세그먼트 삭제
    @Override
    public void deleteSegment(Long segmentId) {
        courseSegmentRepository.deleteById(segmentId);
    }
}
