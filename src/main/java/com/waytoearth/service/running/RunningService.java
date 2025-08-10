package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseRequest;
import com.waytoearth.dto.request.running.RunningResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningPauseResponse;
import com.waytoearth.dto.response.running.RunningResumeResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;

public interface RunningService {
    RunningStartResponse startRunning(Long userId, RunningStartRequest request);
    RunningPauseResponse pauseRunning(Long userId, RunningPauseRequest request);
    RunningResumeResponse resumeRunning(Long userId, RunningResumeRequest request);
    RunningCompleteResponse completeRunning(Long userId, RunningCompleteRequest request);
}
