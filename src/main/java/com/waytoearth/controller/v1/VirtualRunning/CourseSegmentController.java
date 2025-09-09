package com.waytoearth.controller.v1.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CourseSegmentCreateRequest;
import com.waytoearth.dto.response.Virtual.CourseSegmentDetailResponse;
import com.waytoearth.dto.response.Virtual.CourseSegmentSummaryResponse;
import com.waytoearth.service.VirtualRunning.CourseSegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "200", description = "추가 성공")
    @PostMapping("/theme/{themeCourseId}")
    public ResponseEntity<CourseSegmentDetailResponse> addSegmentToTheme(
            @PathVariable Long themeCourseId,
            @Valid @RequestBody CourseSegmentCreateRequest request
    ) {
        return ResponseEntity.ok(courseSegmentService.addSegmentToThemeCourse(themeCourseId, request));
    }

    // ✅ 커스텀 코스 세그먼트 추가
    @Operation(summary = "커스텀 코스에 세그먼트 추가", description = "특정 커스텀 코스에 새로운 세그먼트를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "추가 성공")
    @PostMapping("/custom/{customCourseId}")
    public ResponseEntity<CourseSegmentDetailResponse> addSegmentToCustom(
            @PathVariable Long customCourseId,
            @Valid @RequestBody CourseSegmentCreateRequest request
    ) {
        return ResponseEntity.ok(courseSegmentService.addSegmentToCustomCourse(customCourseId, request));
    }

    // ✅ 테마 코스 세그먼트 목록 조회
    @Operation(summary = "테마 코스 세그먼트 목록 조회", description = "특정 테마 코스에 포함된 모든 세그먼트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/theme/{themeCourseId}")
    public ResponseEntity<List<CourseSegmentSummaryResponse>> getSegmentsByTheme(
            @PathVariable Long themeCourseId
    ) {
        return ResponseEntity.ok(courseSegmentService.getSegmentsByThemeCourse(themeCourseId));
    }

    // ✅ 커스텀 코스 세그먼트 목록 조회
    @Operation(summary = "커스텀 코스 세그먼트 목록 조회", description = "특정 커스텀 코스에 포함된 모든 세그먼트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/custom/{customCourseId}")
    public ResponseEntity<List<CourseSegmentSummaryResponse>> getSegmentsByCustom(
            @PathVariable Long customCourseId
    ) {
        return ResponseEntity.ok(courseSegmentService.getSegmentsByCustomCourse(customCourseId));
    }

    // ✅ 세그먼트 상세 조회
    @Operation(summary = "세그먼트 상세 조회", description = "특정 세그먼트의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{segmentId}")
    public ResponseEntity<CourseSegmentDetailResponse> getSegmentDetail(
            @PathVariable Long segmentId
    ) {
        return ResponseEntity.ok(courseSegmentService.getSegmentDetail(segmentId));
    }

    // ✅ 세그먼트 삭제
    @Operation(summary = "세그먼트 삭제", description = "특정 세그먼트를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{segmentId}")
    public ResponseEntity<Void> deleteSegment(
            @PathVariable Long segmentId
    ) {
        courseSegmentService.deleteSegment(segmentId);
        return ResponseEntity.noContent().build();
    }
}
