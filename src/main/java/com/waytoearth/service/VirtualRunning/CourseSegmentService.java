package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;

import java.util.List;

/**
 * 세그먼트 관리 서비스
 */
public interface CourseSegmentService {
    CourseSegmentDetailResponse addSegment(Long courseId, CourseSegmentCreateRequest request);
    List<CourseSegmentSummaryResponse> getSegments(Long courseId);
    CourseSegmentDetailResponse getSegmentDetail(Long segmentId);
    void deleteSegment(Long segmentId);
}
