package com.waytoearth.dto.request.Virtual;

import com.waytoearth.entity.enums.SegmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "세그먼트 생성 요청 DTO")
public class CourseSegmentCreateRequest {

    @NotNull
    @Schema(description = "세그먼트 타입", example = "DOMESTIC")
    private SegmentType type;

    @NotNull
    @Schema(description = "순서", example = "1")
    private Integer orderIndex;

    @NotNull
    @Schema(description = "시작 위도", example = "37.5665")
    private Double startLat;

    @NotNull
    @Schema(description = "시작 경도", example = "126.9780")
    private Double startLng;

    @NotNull
    @Schema(description = "종료 위도", example = "35.1796")
    private Double endLat;

    @NotNull
    @Schema(description = "종료 경도", example = "129.0756")
    private Double endLng;

    @NotNull
    @Schema(description = "거리 (km)", example = "45.7")
    private Double distanceKm;
}
