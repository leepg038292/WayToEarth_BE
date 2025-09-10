package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;

import java.util.List;



public interface CourseSegmentService {

    // 테마 코스
    CourseSegmentDetailResponse addSegmentToThemeCourse(Long themeCourseId, CourseSegmentCreateRequest request);
    List<CourseSegmentSummaryResponse> getSegmentsByThemeCourse(Long themeCourseId);

    // 커스텀 코스
    CourseSegmentDetailResponse addSegmentToCustomCourse(Long customCourseId, CourseSegmentCreateRequest request);
    List<CourseSegmentSummaryResponse> getSegmentsByCustomCourse(Long customCourseId);

    // 공통
    CourseSegmentDetailResponse getSegmentDetail(Long segmentId);
    void deleteSegment(Long segmentId);
}


