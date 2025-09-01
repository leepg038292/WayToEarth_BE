package com.waytoearth.controller.v1.VirtualRunning;

import com.waytoearth.dto.response.Virtual.ThemeCourseDetailResponse;
import com.waytoearth.dto.response.Virtual.ThemeCourseSummaryResponse;
import com.waytoearth.service.VirtualRunning.ThemeCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/theme-courses")
@RequiredArgsConstructor
@Tag(name = "ThemeCourse", description = "운영자 제공 테마 코스 API")
public class ThemeCourseController {

    private final ThemeCourseService themeCourseService;

    @Operation(summary = "테마 코스 목록 조회", description = "운영자가 제공하는 테마 코스 전체 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<ThemeCourseSummaryResponse>> getThemeCourses() {
        return ResponseEntity.ok(themeCourseService.getThemeCourses());
    }

    @Operation(summary = "테마 코스 상세 조회", description = "특정 테마 코스의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "코스를 찾을 수 없음")
    @GetMapping("/{courseId}")
    public ResponseEntity<ThemeCourseDetailResponse> getThemeCourseDetail(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(themeCourseService.getThemeCourseDetail(courseId));
    }
}
