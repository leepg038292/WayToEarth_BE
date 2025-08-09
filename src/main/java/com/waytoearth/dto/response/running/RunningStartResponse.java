package com.waytoearth.dto.response.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(name = "RunningStartResponse", description = "러닝 시작 응답 DTO")
@Getter
@AllArgsConstructor
public class RunningStartResponse {

    @Schema(description = "세션 ID", example = "2f0a7e9b-3f2b-4c32-8b9a-3d3d7fba8f20")
    private String sessionId;

    @Schema(description = "시작 시각")
    private LocalDateTime startedAt;
}