package com.waytoearth.controller.v1;


import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningUpdateRequest;
import com.waytoearth.dto.response.running.*;
import com.waytoearth.service.running.RunningService;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Running", description = "러닝 세션 API")
@RestController
@RequestMapping("/v1/running")
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    @Operation(summary = "러닝 시작", description = "세션 생성 및 sessionId 발급")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/start")
    public ResponseEntity<RunningStartResponse> start(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningStartRequest request
    ) {
        return ResponseEntity.ok(runningService.startRunning(user, request));
    }

    @Operation(summary = "러닝 주기 업데이트", description = "거리/시간/페이스/칼로리 + 현재 좌표 1개 저장")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/update")
    public ResponseEntity<RunningUpdateResponse> update(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningUpdateRequest request
    ) {
        return ResponseEntity.ok(runningService.updateRunning(user, request));
    }

    @Operation(summary = "러닝 일시정지", description = "상태를 PAUSED로 전환")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/pause")
    public ResponseEntity<RunningPauseResumeResponse> pause(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request
    ) {
        return ResponseEntity.ok(runningService.pauseRunning(user, request));
    }

    @Operation(summary = "러닝 재개", description = "상태를 RUNNING으로 전환")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/resume")
    public ResponseEntity<RunningPauseResumeResponse> resume(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request
    ) {
        return ResponseEntity.ok(runningService.resumeRunning(user, request));
    }

    @Operation(summary = "러닝 완료", description = "최종 데이터 저장 및 완료 페이지 데이터 반환")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/complete")
    public ResponseEntity<RunningCompleteResponse> complete(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningCompleteRequest request
    ) {
        return ResponseEntity.ok(runningService.completeRunning(user, request));
    }

    @Operation(summary = "러닝 기록 목록", description = "완료된 기록 목록 반환")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/records")
    public ResponseEntity<List<RunningRecordSummaryResponse>> records(
            @AuthUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(runningService.getRecords(user));
    }
}
