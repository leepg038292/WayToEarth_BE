package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;
import com.waytoearth.entity.VirtualRunning.CourseSegmentEntity;
import com.waytoearth.repository.VirtualRunning.CourseSegmentRepository;
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
    private final CustomCourseRepository customCourseRepository;

    @Override
    public CourseSegmentDetailResponse addSegment(Long courseId, CourseSegmentCreateRequest request) {
        var course = customCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));

        var segment = CourseSegmentEntity.builder()
                .course(course)
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

    @Override
    public List<CourseSegmentSummaryResponse> getSegments(Long courseId) {
        return courseSegmentRepository.findByCourseIdOrderByOrderIndex(courseId)
                .stream()
                .map(seg -> new CourseSegmentSummaryResponse(
                        seg.getId(),
                        seg.getOrderIndex(),
                        seg.getDistanceKm()
                ))
                .toList();
    }

    @Override
    public CourseSegmentDetailResponse getSegmentDetail(Long segmentId) {
        var seg = courseSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("세그먼트를 찾을 수 없습니다."));
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

    @Override
    public void deleteSegment(Long segmentId) {
        courseSegmentRepository.deleteById(segmentId);
    }
}
