package com.waytoearth.dto.response.running;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "러닝 시작 응답")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningStartResponse {

    @Schema(description = "러닝 세션 ID", example = "b6b2b8b5-2d8d-4e8f-9a8e-7e6c5d4f3a21")
    private String sessionId;

    @Schema(description = "러닝 시작 시각(한국시간 ISO-8601)", example = "2025-08-10T09:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startedAt;
}
