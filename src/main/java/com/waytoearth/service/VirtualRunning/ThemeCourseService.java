package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.response.Virtual.ThemeCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.ThemeCourseSummaryResponse;

import java.util.List;

/**
 * 운영자 제공 테마 코스 서비스
 */
public interface ThemeCourseService {
    List<ThemeCourseSummaryResponse> getThemeCourses();
    ThemeCourseDetailResponse getThemeCourseDetail(Long courseId);
}
