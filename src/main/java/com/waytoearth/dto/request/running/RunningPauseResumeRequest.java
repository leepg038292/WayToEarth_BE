package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RunningPauseResumeRequest {
    @Schema(description = "세션 ID", example = "abc123")
    private String sessionId;
}