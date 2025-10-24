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
@Schema(description = "가입 신청 처리 요청 (승인/거부)")
public class JoinRequestProcessRequest {

    @Size(max = 500, message = "처리 메모는 500자를 초과할 수 없습니다.")
    @Schema(description = "처리 메모", example = "환영합니다! / 죄송하지만 현재 정원이 가득 찼습니다.")
    private String note;
}