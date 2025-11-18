package com.waytoearth.dto.request.running;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "페이스 코치 체크 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaceCoachCheckRequest {

    @Schema(description = "러닝 세션 ID", example = "running-session-123", required = true)
    @NotBlank(message = "세션 ID는 필수입니다")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "현재 통과한 km", example = "2", required = true)
    @NotNull(message = "현재 km는 필수입니다")
    @Min(value = 1, message = "km는 1 이상이어야 합니다")
    @JsonProperty("current_km")
    private Integer currentKm;

    @Schema(description = "현재 페이스 (초/km)", example = "380", required = true)
    @NotNull(message = "현재 페이스는 필수입니다")
    @Min(value = 1, message = "페이스는 1초 이상이어야 합니다")
    @JsonProperty("current_pace_seconds")
    private Integer currentPaceSeconds;
}
