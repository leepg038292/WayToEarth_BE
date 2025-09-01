package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CustomCourseCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CustomCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.CustomCourseSummaryResponse;
import com.waytoearth.entity.VirtualRunning.CustomCourseEntity;
import com.waytoearth.repository.VirtualRunning.CustomCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomCourseServiceImpl implements CustomCourseService {

    private final CustomCourseRepository customCourseRepository;

    @Override
    public CustomCourseDetailResponse createCourse(CustomCourseCreateRequest request) {
        CustomCourseEntity course = CustomCourseEntity.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .totalDistanceKm(request.getSegments().stream()
                        .mapToDouble(CourseSegmentCreateRequest::getDistanceKm)
                        .sum())
                .createdAt(LocalDateTime.now())
                .build();

        CustomCourseEntity saved = customCourseRepository.save(course);

        return new CustomCourseDetailResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getTotalDistanceKm(),
                List.of() // TODO: 세그먼트 저장 후 매핑
        );
    }

    @Override
    public List<CustomCourseSummaryResponse> getUserCourses(Long userId) {
        return customCourseRepository.findByUserId(userId).stream()
                .map(course -> new CustomCourseSummaryResponse(
                        course.getId(),
                        course.getTitle(),
                        course.getTotalDistanceKm()
                ))
                .toList();
    }

    @Override
    public CustomCourseDetailResponse getCourseDetail(Long courseId) {
        CustomCourseEntity course = customCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("커스텀 코스를 찾을 수 없습니다. id=" + courseId));

        return new CustomCourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getTotalDistanceKm(),
                course.getSegments().stream()
                        .map(segment -> new CourseSegmentDetailResponse(
                                segment.getId(),
                                segment.getType().name(),
                                segment.getStartLat(),
                                segment.getStartLng(),
                                segment.getEndLat(),
                                segment.getEndLng(),
                                segment.getDistanceKm()
                        ))
                        .toList()
        );
    }

    @Override
    public void deleteCourse(Long courseId, Long userId) {
        CustomCourseEntity course = customCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));

        if (!course.getUserId().equals(userId)) {
            throw new IllegalStateException("본인 소유 코스만 삭제할 수 있습니다.");
        }

        customCourseRepository.delete(course);
    }
}
