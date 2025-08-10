package com.waytoearth.dto.response.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "러닝 일시정지 응답(스냅샷)")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningPauseResponse {

    private String sessionId;

    private Integer distanceMeters;

    private Integer durationSeconds;

    @Schema(description = "평균 페이스(초/킬로미터)")
    private Integer averagePaceSeconds;

    @Schema(description = "칼로리(kcal)")
    private Integer calories;

    private LocalDateTime pausedAt;
}
