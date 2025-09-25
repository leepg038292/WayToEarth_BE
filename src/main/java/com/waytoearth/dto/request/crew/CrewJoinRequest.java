package com.waytoearth.dto.request.crew;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 가입 신청 요청")
public class CrewJoinRequest {

    @Size(max = 500, message = "가입 메시지는 500자를 초과할 수 없습니다.")
    @Schema(description = "가입 신청 메시지", example = "안녕하세요! 함께 러닝하고 싶습니다.")
    private String message;
}