package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunningTitleUpdateRequest {
    @Schema(description = "새 제목", example = "금요일 오전 러닝")
    private String title;
}
