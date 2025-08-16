package com.waytoearth.dto.response.running;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@AllArgsConstructor
public class RunningPauseResumeResponse {
    @Schema(description = "처리 여부", example = "true")
    private boolean ack;

    @Schema(description = "현재 상태", example = "PAUSED")
    private String status;
}