package com.waytoearth.dto.response.feed;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    @Schema(description = "응답 메시지", example = "피드가 삭제되었습니다.")
    private String message;
}
