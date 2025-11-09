package com.waytoearth.dto.request.auth;

import com.waytoearth.entity.enums.AgeGroup;
import com.waytoearth.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "온보딩 완료 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    @Schema(description = "닉네임", example = "홍러너", required = true)
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
    private String nickname;

    @Schema(description = "거주지", example = "서울특별시 강남구", required = true)
    @NotBlank(message = "거주지는 필수입니다")
    @Size(max = 100, message = "거주지는 100자 이하여야 합니다")
    private String residence;

    @Schema(description = "연령대", example = "TWENTIES", required = true)
    @NotNull(message = "연령대는 필수입니다")
    private AgeGroup age_group;

    @Schema(description = "성별", example = "MALE", required = true)
    @NotNull(message = "성별은 필수입니다")
    private Gender gender;

    @Schema(description = "키(cm)", example = "175", required = true)
    @NotNull(message = "키는 필수입니다")
    @Min(value = 100, message = "키는 100cm 이상이어야 합니다")
    @Max(value = 250, message = "키는 250cm 이하여야 합니다")
    private Integer height;

    @Schema(description = "몸무게(kg)", example = "70", required = true)
    @NotNull(message = "몸무게는 필수입니다")
    @Min(value = 30, message = "몸무게는 30kg 이상이어야 합니다")
    @Max(value = 200, message = "몸무게는 200kg 이하여야 합니다")
    private Integer weight;

    @Schema(description = "주간 목표 거리(km)", example = "15.5", required = true)
    @NotNull(message = "주간 목표 거리는 필수입니다")
    @DecimalMin(value = "0.1", message = "주간 목표 거리는 0.1km 이상이어야 합니다")
    @DecimalMax(value = "999.99", message = "주간 목표 거리는 999.99km 이하여야 합니다")
    private BigDecimal weekly_goal_distance;

    @Schema(description = "프로필 이미지 S3 Key (Presigned URL 응답의 key 사용)", example = "profiles/123/profile_1234567890.jpg")
    @Size(max = 1024, message = "프로필 이미지 Key는 1024자 이하여야 합니다")
    private String profileImageKey;
}