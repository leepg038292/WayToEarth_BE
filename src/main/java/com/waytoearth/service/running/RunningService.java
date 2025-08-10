package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;

public interface RunningService {
    RunningStartResponse start(String kakaoId, RunningStartRequest req);
    RunningCompleteResponse complete(String kakaoId, RunningCompleteRequest req);
}
