package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.request.Virtual.UserVirtualCourseCreateRequest;
import com.waytoearth.dto.request.Virtual.VirtualCourseProgressUpdateRequest;
import com.waytoearth.dto.response.Virtual.SegmentProgressResponse;
import com.waytoearth.dto.response.Virtual.UserVirtualCourseResponse;
import com.waytoearth.dto.response.Virtual.VirtualCourseProgressResponse;

import java.util.List;

public interface UserVirtualCourseService {
    VirtualCourseProgressResponse updateProgress(Long userVirtualCourseId, VirtualCourseProgressUpdateRequest request);
    VirtualCourseProgressResponse getProgress(Long userVirtualCourseId);
    List<SegmentProgressResponse> getSegmentProgress(Long userVirtualCourseId);
    UserVirtualCourseResponse createUserVirtualCourse(UserVirtualCourseCreateRequest request);
}
