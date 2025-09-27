package com.waytoearth.dto.request.crew;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    @Size(max = 50, message = "크루 이름은 50자를 초과할 수 없습니다.")
    @Schema(description = "크루 이름", example = "서울 러닝 크루", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "크루 소개는 500자를 초과할 수 없습니다.")
    @Schema(description = "크루 소개", example = "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다")
    private String description;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "최대 100명까지 가능합니다.")
    @Schema(description = "최대 인원", example = "20")
    private Integer maxMembers = 50;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;
}