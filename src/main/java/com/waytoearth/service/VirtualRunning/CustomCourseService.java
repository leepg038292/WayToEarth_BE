package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CustomCourseCreateRequest;
import com.waytoearth.dto.response.Virtual.CustomCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.CustomCourseSummaryResponse;

import java.util.List;

/**
 * 사용자 커스텀 코스 서비스
 */
public interface CustomCourseService {
    CustomCourseDetailResponse createCourse(CustomCourseCreateRequest request);
    List<CustomCourseSummaryResponse> getUserCourses(Long userId);
    CustomCourseDetailResponse getCourseDetail(Long courseId);
    void deleteCourse(Long courseId, Long userId);
}
