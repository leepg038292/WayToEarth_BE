package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Schema(description = "러닝 일시정지 요청")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningPauseRequest {

    @Schema(description = "세션 ID", example = "b6b2b8b5-2d8d-4e8f-9a8e-7e6c5d4f3a21", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String sessionId;

    @Schema(description = "현재까지 이동 거리(미터)", example = "1250", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero
    private Integer distanceMeters;

    @Schema(description = "현재까지 소요 시간(초)", example = "420", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    private Integer durationSeconds;

    @Schema(description = "현재까지 경로 포인트(선택)")
    @Valid
    private List<RunningCompleteRequest.RoutePoint> routePoints;
}
