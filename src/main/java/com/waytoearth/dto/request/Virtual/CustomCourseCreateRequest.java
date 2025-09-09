package com.waytoearth.dto.request.Virtual;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "커스텀 코스 생성 요청 DTO")
public class CustomCourseCreateRequest {

    @NotNull
    @Schema(description = "사용자 ID", example = "42")
    private Long userId;

    @NotBlank
    @Schema(description = "코스 제목", example = "한강 러닝 코스")
    private String title;

    @NotNull
    @Schema(description = "세그먼트 배열")
    private List<CourseSegmentCreateRequest> segments;
}
