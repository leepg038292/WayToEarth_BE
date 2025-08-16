package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningPauseResumeRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningUpdateRequest;
import com.waytoearth.dto.response.running.*;
import com.waytoearth.security.AuthenticatedUser;

import java.util.List;

public interface RunningService {
    RunningStartResponse startRunning(AuthenticatedUser user, RunningStartRequest request);
    RunningUpdateResponse updateRunning(AuthenticatedUser user, RunningUpdateRequest request);
    RunningPauseResumeResponse pauseRunning(AuthenticatedUser user, RunningPauseResumeRequest request);
    RunningPauseResumeResponse resumeRunning(AuthenticatedUser user, RunningPauseResumeRequest request);
    RunningCompleteResponse completeRunning(AuthenticatedUser user, RunningCompleteRequest request);
    List<RunningRecordSummaryResponse> getRecords(AuthenticatedUser user);
}