package com.waytoearth.controller.v1;


import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningUpdateRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningPauseResumeResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.dto.response.running.RunningUpdateResponse;
import com.waytoearth.service.running.RunningService;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/running")
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    @PostMapping("/start")
    public ResponseEntity<RunningStartResponse> startRunning(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningStartRequest request) {
        return ResponseEntity.ok(runningService.startRunning(user, request));
    }

    @PostMapping("/update")
    public ResponseEntity<RunningUpdateResponse> updateRunning(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningUpdateRequest request) {
        return ResponseEntity.ok(runningService.updateRunning(user, request));
    }

    @PostMapping("/pause")
    public ResponseEntity<RunningPauseResumeResponse> pauseRunning(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        return ResponseEntity.ok(runningService.pauseRunning(user, request));
    }

    @PostMapping("/resume")
    public ResponseEntity<RunningPauseResumeResponse> resumeRunning(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningPauseResumeRequest request) {
        return ResponseEntity.ok(runningService.resumeRunning(user, request));
    }

    @PostMapping("/complete")
    public ResponseEntity<RunningCompleteResponse> completeRunning(
            @AuthUser AuthenticatedUser user,
            @RequestBody RunningCompleteRequest request) {
        return ResponseEntity.ok(runningService.completeRunning(user, request));
    }
}
