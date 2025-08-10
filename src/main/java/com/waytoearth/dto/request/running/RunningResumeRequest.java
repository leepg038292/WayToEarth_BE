package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "러닝 재개 요청")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningResumeRequest {

    @Schema(description = "세션 ID", example = "b6b2b8b5-2d8d-4e8f-9a8e-7e6c5d4f3a21", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String sessionId;
}
