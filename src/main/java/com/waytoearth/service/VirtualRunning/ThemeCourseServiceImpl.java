package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.ThemeCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.ThemeCourseSummaryResponse;
import com.waytoearth.repository.VirtualRunning.ThemeCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeCourseServiceImpl implements ThemeCourseService {

    private final ThemeCourseRepository themeCourseRepository;

    @Override
    public List<ThemeCourseSummaryResponse> getThemeCourses() {
        return themeCourseRepository.findAll().stream()
                .map(course -> new ThemeCourseSummaryResponse(
                        course.getId(),
                        course.getTitle(),
                        course.getTotalDistanceKm()
                ))
                .toList();
    }

    @Override
    public ThemeCourseDetailResponse getThemeCourseDetail(Long courseId) {
        // ✅ N+1 문제 해결: fetch join으로 세그먼트까지 한번에 조회
        var course = themeCourseRepository.findByIdWithSegments(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다. id=" + courseId));

        return new ThemeCourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
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
}
