package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.User.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 요약 정보")
public record UserSummaryResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "닉네임", example = "러너123")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
            user.getId(),
            user.getNickname(),
            user.getProfileImageUrl()
        );
    }
}