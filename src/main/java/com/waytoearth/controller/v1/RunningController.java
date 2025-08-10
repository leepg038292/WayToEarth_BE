package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.running.RunningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Running", description = "러닝 시작 및 완료 API")
@RestController
@RequestMapping("/v1/running")
@RequiredArgsConstructor
@Validated
public class RunningController {

    private final RunningService runningService;

    @Operation(summary = "러닝 시작", description = "세션 ID와 타입을 받아 러닝을 시작합니다.")
    @PostMapping("/start")
    public ResponseEntity<RunningStartResponse> startRunning(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody RunningStartRequest request
    ) {
        RunningStartResponse response = runningService.startRunning(user.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "러닝 완료", description = "거리, 시간, 경로 데이터를 받아 러닝을 완료합니다.")
    @PostMapping("/complete")
    public ResponseEntity<RunningCompleteResponse> completeRunning(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody RunningCompleteRequest request
    ) {
        RunningCompleteResponse response = runningService.completeRunning(user.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}
