package com.waytoearth.dto.request.running;

import com.waytoearth.entity.enums.RunningType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "러닝 시작 요청")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningStartRequest {

    @Schema(description = "러닝 세션 ID (클라이언트 또는 서버 생성)", example = "b6b2b8b5-2d8d-4e8f-9a8e-7e6c5d4f3a21", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "sessionId는 필수입니다.")
    private String sessionId;

    @Schema(description = "러닝 타입", example = "SINGLE", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"SINGLE", "JOURNEY"})
    @NotNull(message = "runningType은 필수입니다.")
    private RunningType runningType;

}
