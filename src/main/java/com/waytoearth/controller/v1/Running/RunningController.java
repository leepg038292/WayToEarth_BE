package com.waytoearth.controller.v1.Running;


import com.waytoearth.dto.request.running.*;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.common.PagedResponse;
import com.waytoearth.dto.response.running.*;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.running.RunningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/running")
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    @Operation(summary = "러닝 시작")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RunningStartResponse>> start(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningStartRequest request) {
        RunningStartResponse response = runningService.startRunning(user, request);
        return ResponseEntity.ok(ApiResponse.success(response, "러닝이 성공적으로 시작되었습니다."));
    }

    @Operation(summary = "러닝 중 주기 업데이트")
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<RunningUpdateResponse>> update(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningUpdateRequest request) {
        runningService.updateRunning(user, request);
        RunningUpdateResponse response = new RunningUpdateResponse(true);
        return ResponseEntity.ok(ApiResponse.success(response, "러닝 데이터가 성공적으로 업데이트되었습니다."));
    }

    @Operation(summary = "러닝 일시정지")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<RunningPauseResumeResponse>> pause(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        runningService.pauseRunning(user, request);
        RunningPauseResumeResponse response = new RunningPauseResumeResponse(true, "PAUSED");
        return ResponseEntity.ok(ApiResponse.success(response, "러닝이 일시정지되었습니다."));
    }

    @Operation(summary = "러닝 재개")
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<RunningPauseResumeResponse>> resume(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        runningService.resumeRunning(user, request);
        RunningPauseResumeResponse response = new RunningPauseResumeResponse(true, "RUNNING");
        return ResponseEntity.ok(ApiResponse.success(response, "러닝이 재개되었습니다."));
    }

    @Operation(summary = "러닝 완료")
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<RunningCompleteResponse>> complete(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningCompleteRequest request) {
        RunningCompleteResponse response = runningService.completeRunning(user, request);
        return ResponseEntity.ok(ApiResponse.success(response, "러닝이 성공적으로 완료되었습니다."));
    }

    @Operation(summary = "러닝 제목 수정")
    @PatchMapping("/{recordId}/title")
    public ResponseEntity<ApiResponse<Void>> updateTitle(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long recordId,
            @RequestBody RunningTitleUpdateRequest request) {
        runningService.updateTitle(user, recordId, request);
        return ResponseEntity.ok(ApiResponse.success("러닝 제목이 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "러닝 상세 조회(경로 포함)")
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<RunningCompleteResponse>> detail(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long recordId) {
        RunningCompleteResponse response = runningService.getDetail(user, recordId);
        return ResponseEntity.ok(ApiResponse.success(response, "러닝 상세 정보를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "러닝 기록 목록 조회 (페이징)")
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<PagedResponse<RunningRecordSummaryResponse>>> getRecords(
            @AuthUser AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RunningRecordSummaryResponse> pageResult = runningService.getRecords(user, pageable);
        
        PagedResponse<RunningRecordSummaryResponse> pagedData = PagedResponse.of(pageResult);
        return ResponseEntity.ok(ApiResponse.success(pagedData, "러닝 기록 목록을 성공적으로 조회했습니다."));
    }




}

