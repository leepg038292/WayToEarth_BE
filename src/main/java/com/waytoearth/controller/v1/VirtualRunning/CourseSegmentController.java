package com.waytoearth.controller.v1.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.service.VirtualRunning.CourseSegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/course-segments")
@RequiredArgsConstructor
@Tag(name = "CourseSegment", description = "코스 세그먼트 관리 API")
public class CourseSegmentController {

    private final CourseSegmentService courseSegmentService;

    // ✅ 테마 코스 세그먼트 추가
    @Operation(summary = "테마 코스에 세그먼트 추가", description = "특정 테마 코스에 새로운 세그먼트를 추가합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 성공")
    @PostMapping("/theme/{themeCourseId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<CourseSegmentDetailResponse>> addSegmentToTheme(
            @PathVariable Long themeCourseId,
            @Valid @RequestBody CourseSegmentCreateRequest request
    ) {
        CourseSegmentDetailResponse response = courseSegmentService.addSegmentToThemeCourse(themeCourseId, request);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "테마 코스에 세그먼트가 성공적으로 추가되었습니다."));
    }

    // ✅ 커스텀 코스 세그먼트 추가
    @Operation(summary = "커스텀 코스에 세그먼트 추가", description = "특정 커스텀 코스에 새로운 세그먼트를 추가합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 성공")
    @PostMapping("/custom/{customCourseId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<CourseSegmentDetailResponse>> addSegmentToCustom(
            @PathVariable Long customCourseId,
            @Valid @RequestBody CourseSegmentCreateRequest request
    ) {
        CourseSegmentDetailResponse response = courseSegmentService.addSegmentToCustomCourse(customCourseId, request);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "커스텀 코스에 세그먼트가 성공적으로 추가되었습니다."));
    }

    // ✅ 테마 코스 세그먼트 목록 조회
    @Operation(summary = "테마 코스 세그먼트 목록 조회", description = "특정 테마 코스에 포함된 모든 세그먼트를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/theme/{themeCourseId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<List<CourseSegmentSummaryResponse>>> getSegmentsByTheme(
            @PathVariable Long themeCourseId
    ) {
        List<CourseSegmentSummaryResponse> response = courseSegmentService.getSegmentsByThemeCourse(themeCourseId);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "테마 코스 세그먼트 목록을 성공적으로 조회했습니다."));
    }

    // ✅ 커스텀 코스 세그먼트 목록 조회
    @Operation(summary = "커스텀 코스 세그먼트 목록 조회", description = "특정 커스텀 코스에 포함된 모든 세그먼트를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/custom/{customCourseId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<List<CourseSegmentSummaryResponse>>> getSegmentsByCustom(
            @PathVariable Long customCourseId
    ) {
        List<CourseSegmentSummaryResponse> response = courseSegmentService.getSegmentsByCustomCourse(customCourseId);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "커스텀 코스 세그먼트 목록을 성공적으로 조회했습니다."));
    }

    // ✅ 세그먼트 상세 조회
    @Operation(summary = "세그먼트 상세 조회", description = "특정 세그먼트의 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{segmentId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<CourseSegmentDetailResponse>> getSegmentDetail(
            @PathVariable Long segmentId
    ) {
        CourseSegmentDetailResponse response = courseSegmentService.getSegmentDetail(segmentId);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "세그먼트 상세 정보를 성공적으로 조회했습니다."));
    }

    // ✅ 세그먼트 삭제
    @Operation(summary = "세그먼트 삭제", description = "특정 세그먼트를 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{segmentId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<Void>> deleteSegment(
            @PathVariable Long segmentId
    ) {
        courseSegmentService.deleteSegment(segmentId);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success("세그먼트가 성공적으로 삭제되었습니다."));
    }
}
