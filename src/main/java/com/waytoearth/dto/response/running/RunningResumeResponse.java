package com.waytoearth.dto.response.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "러닝 재개 응답")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningResumeResponse {

    private String sessionId;

    private LocalDateTime resumedAt;
}
