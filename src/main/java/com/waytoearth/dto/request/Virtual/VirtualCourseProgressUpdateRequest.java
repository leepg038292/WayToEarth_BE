package com.waytoearth.dto.request.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "가상 코스 진행률 업데이트 요청 DTO")
public class VirtualCourseProgressUpdateRequest {

    @NotNull
    @Schema(description = "세그먼트 ID", example = "100")
    private Long segmentId;

    @NotNull
    @Schema(description = "추가 거리 (km)", example = "2.5")
    private Double distanceKm;
}
