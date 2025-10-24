package com.waytoearth.dto.request.running.ai;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningAnalysisRequest {

    @NotNull(message = "러닝 기록 ID는 필수입니다")
    private Long runningRecordId;
}
