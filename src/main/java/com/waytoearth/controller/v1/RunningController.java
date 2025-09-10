package com.waytoearth.controller.v1;


import com.waytoearth.dto.request.running.*;
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

import java.util.Map;

@RestController
@RequestMapping("/v1/running")
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    @Operation(summary = "러닝 시작")
    @PostMapping("/start")
    public ResponseEntity<RunningStartResponse> start(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningStartRequest request) {
        return ResponseEntity.ok(runningService.startRunning(user, request));
    }

    @Operation(summary = "러닝 중 주기 업데이트")
    @PostMapping("/update")
    public ResponseEntity<RunningUpdateResponse> update(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningUpdateRequest request) {
        runningService.updateRunning(user, request);
        return ResponseEntity.ok(new RunningUpdateResponse(true));
    }

    @Operation(summary = "러닝 일시정지")
    @PostMapping("/pause")
    public ResponseEntity<RunningPauseResumeResponse> pause(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        runningService.pauseRunning(user, request);
        return ResponseEntity.ok(new RunningPauseResumeResponse(true, "PAUSED"));
    }

    @Operation(summary = "러닝 재개")
    @PostMapping("/resume")
    public ResponseEntity<RunningPauseResumeResponse> resume(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        runningService.resumeRunning(user, request);
        return ResponseEntity.ok(new RunningPauseResumeResponse(true, "RUNNING"));
    }

    @Operation(summary = "러닝 완료")
    @PostMapping("/complete")
    public ResponseEntity<RunningCompleteResponse> complete(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningCompleteRequest request) {
        return ResponseEntity.ok(runningService.completeRunning(user, request));
    }

    @Operation(summary = "러닝 제목 수정")
    @PatchMapping("/{recordId}/title")
    public ResponseEntity<Void> updateTitle(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long recordId,
            @RequestBody RunningTitleUpdateRequest request) {
        runningService.updateTitle(user, recordId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "러닝 상세 조회(경로 포함)")
    @GetMapping("/{recordId}")
    public ResponseEntity<RunningCompleteResponse> detail(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long recordId) {
        return ResponseEntity.ok(runningService.getDetail(user, recordId));
    }

    @Operation(summary = "러닝 기록 목록 조회 (페이징)")
    @GetMapping("/records")
    public ResponseEntity<?> getRecords(
            @AuthUser AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RunningRecordSummaryResponse> pageResult = runningService.getRecords(user, pageable);

        // ✅ 직렬화 가능한 Map으로 감싸서 반환
        return ResponseEntity.ok(Map.of(
                "content", pageResult.getContent(),
                "page", pageResult.getNumber(),
                "size", pageResult.getSize(),
                "totalElements", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages()
        ));
    }




}

