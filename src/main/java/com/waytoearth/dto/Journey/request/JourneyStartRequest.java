package com.waytoearth.dto.Journey.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "여행 시작 요청")
public record JourneyStartRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @NotNull(message = "여행 ID는 필수입니다")
    @Schema(description = "여행 ID", example = "1")
    Long journeyId
) {}