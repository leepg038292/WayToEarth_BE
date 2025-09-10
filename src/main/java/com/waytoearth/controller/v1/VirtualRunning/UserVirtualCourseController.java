package com.waytoearth.controller.v1.VirtualRunning;

import com.waytoearth.dto.request.Virtual.UserVirtualCourseCreateRequest;
import com.waytoearth.dto.request.Virtual.VirtualCourseProgressUpdateRequest;
import com.waytoearth.dto.response.Virtual.*;
import com.waytoearth.service.VirtualRunning.SegmentEmblemService;
import com.waytoearth.service.VirtualRunning.SegmentLandmarkService;
import com.waytoearth.service.VirtualRunning.SegmentWeatherService;
import com.waytoearth.service.VirtualRunning.UserVirtualCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/user-virtual-courses")
@RequiredArgsConstructor
@Tag(name = "UserVirtualCourse", description = "사용자 가상 코스 진행 API")
public class UserVirtualCourseController {

    private final UserVirtualCourseService userVirtualCourseService;
    private final SegmentWeatherService segmentWeatherService;
    private final SegmentLandmarkService segmentLandmarkService;
    private final SegmentEmblemService segmentEmblemService;

    @Operation(summary = "코스 진행률 업데이트", description = "사용자의 러닝 기록을 반영하여 진행률을 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @PostMapping("/{userVirtualCourseId}/progress")
    public ResponseEntity<VirtualCourseProgressResponse> updateProgress(
            @PathVariable Long userVirtualCourseId,
            @Valid @RequestBody VirtualCourseProgressUpdateRequest request
    ) {
        return ResponseEntity.ok(userVirtualCourseService.updateProgress(userVirtualCourseId, request));
    }

    @Operation(summary = "코스 전체 진행률 조회", description = "사용자의 전체 가상 코스 진행률을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{userVirtualCourseId}/progress")
    public ResponseEntity<VirtualCourseProgressResponse> getProgress(
            @PathVariable Long userVirtualCourseId
    ) {
        return ResponseEntity.ok(userVirtualCourseService.getProgress(userVirtualCourseId));
    }

    @Operation(summary = "세그먼트 진행률 조회", description = "사용자의 특정 가상 코스에서 세그먼트별 진행률을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{userVirtualCourseId}/segments")
    public ResponseEntity<List<SegmentProgressResponse>> getSegmentProgress(
            @PathVariable Long userVirtualCourseId
    ) {
        return ResponseEntity.ok(userVirtualCourseService.getSegmentProgress(userVirtualCourseId));
    }

    @Operation(summary = "세그먼트 날씨 조회", description = "특정 세그먼트의 날씨 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{userVirtualCourseId}/segments/{segmentId}/weather")
    public ResponseEntity<SegmentWeatherResponse> getWeather(
            @PathVariable Long userVirtualCourseId,
            @PathVariable Long segmentId
    ) {
        return ResponseEntity.ok(segmentWeatherService.getWeather(segmentId));
    }

    @Operation(summary = "세그먼트 랜드마크 조회", description = "특정 세그먼트의 랜드마크 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{userVirtualCourseId}/segments/{segmentId}/landmarks")
    public ResponseEntity<List<SegmentLandmarkResponse>> getLandmarks(
            @PathVariable Long userVirtualCourseId,
            @PathVariable Long segmentId
    ) {
        return ResponseEntity.ok(segmentLandmarkService.getLandmarks(segmentId));
    }

    @Operation(summary = "세그먼트 엠블럼 확인", description = "특정 세그먼트에서 달성한 엠블럼을 확인합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{userVirtualCourseId}/segments/{segmentId}/emblems")
    public ResponseEntity<SegmentEmblemResponse> getEmblem(
            @PathVariable Long userVirtualCourseId,
            @PathVariable Long segmentId,
            @RequestParam Double distance
    ) {
        return ResponseEntity.ok(segmentEmblemService.checkEmblem(userVirtualCourseId, segmentId, distance));
    }


    @Operation(summary = "사용자 가상 코스 등록", description = "사용자가 특정 코스를 선택하여 가상 러닝 코스를 시작합니다.")
    @ApiResponse(responseCode = "200", description = "등록 성공")
    @PostMapping
    public ResponseEntity<UserVirtualCourseResponse> createUserVirtualCourse(
            @Valid @RequestBody UserVirtualCourseCreateRequest request
    ) {
        return ResponseEntity.ok(userVirtualCourseService.createUserVirtualCourse(request));
    }

}
