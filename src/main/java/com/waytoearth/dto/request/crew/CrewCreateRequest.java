package com.waytoearth.dto.request.crew;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 생성 요청")
public class CrewCreateRequest {

    @NotBlank(message = "크루 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "크루 이름은 2~50자여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9 _-]+$", message = "크루 이름은 한글, 영문, 숫자, 공백, '-', '_'만 가능합니다.")
    @Schema(description = "크루 이름", example = "서울 러닝 크루", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "크루 소개는 500자를 초과할 수 없습니다.")
    @Schema(description = "크루 소개", example = "함께 달리며 건강과 라이프스타일을 추구하는 크루입니다")
    private String description;

    @Min(value = 2, message = "크루 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 1000, message = "크루 인원은 최대 1000명까지 가능합니다.")
    @Schema(description = "최대 인원", example = "20")
    private Integer maxMembers = 50;

    @Pattern(regexp = "^https?://.*", message = "올바른 URL 형식이 아닙니다.")
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;
}

