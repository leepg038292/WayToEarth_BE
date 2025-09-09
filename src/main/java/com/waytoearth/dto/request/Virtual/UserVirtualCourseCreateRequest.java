package com.waytoearth.dto.request.Virtual;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 가상 코스 등록 요청 DTO")
public record UserVirtualCourseCreateRequest(

        @NotNull
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @NotNull
        @Schema(description = "코스 ID (ThemeCourse 또는 CustomCourse)", example = "10")
        Long courseId,

        @NotNull
        @Schema(description = "코스 타입", example = "CUSTOM")
        String courseType
) {}
