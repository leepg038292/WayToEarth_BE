package com.waytoearth.controller.v1.VirtualRunning;

import com.waytoearth.dto.request.Virtual.CustomCourseCreateRequest;
import com.waytoearth.dto.response.Virtual.CustomCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.CustomCourseSummaryResponse;
import com.waytoearth.service.VirtualRunning.CustomCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/custom-courses")
@RequiredArgsConstructor
@Tag(name = "CustomCourse", description = "사용자 커스텀 코스 API")
public class CustomCourseController {

    private final CustomCourseService customCourseService;

    @Operation(summary = "커스텀 코스 생성", description = "사용자가 직접 코스를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    public ResponseEntity<CustomCourseDetailResponse> createCourse(
            @Valid @RequestBody CustomCourseCreateRequest request
    ) {
        return ResponseEntity.ok(customCourseService.createCourse(request));
    }

    @Operation(summary = "사용자 커스텀 코스 목록 조회", description = "특정 사용자가 등록한 모든 커스텀 코스를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CustomCourseSummaryResponse>> getUserCourses(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(customCourseService.getUserCourses(userId));
    }

    @Operation(summary = "커스텀 코스 상세 조회", description = "특정 커스텀 코스의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{courseId}")
    public ResponseEntity<CustomCourseDetailResponse> getCourseDetail(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(customCourseService.getCourseDetail(courseId));
    }

    @Operation(summary = "커스텀 코스 삭제", description = "사용자가 등록한 커스텀 코스를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{courseId}/user/{userId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @PathVariable Long userId
    ) {
        customCourseService.deleteCourse(courseId, userId);
        return ResponseEntity.noContent().build();
    }
}
