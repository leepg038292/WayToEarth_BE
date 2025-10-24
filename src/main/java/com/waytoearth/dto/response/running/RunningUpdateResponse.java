package com.waytoearth.dto.response.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RunningUpdateResponse {
    @Schema(description = "저장 성공 여부", example = "true")
    private boolean ack;
}