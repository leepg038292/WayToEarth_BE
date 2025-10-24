package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.user.User;
import com.waytoearth.service.file.FileService;
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
    public static UserSummaryResponse from(User user, FileService fileService) {
        String profileImageKey = user != null ? user.getProfileImageKey() : null;
        String profileImageUrl = (profileImageKey != null && !profileImageKey.isEmpty())
                ? fileService.createPresignedGetUrl(profileImageKey)
                : null;

        return new UserSummaryResponse(
            user != null ? user.getId() : null,
            user != null ? user.getNickname() : null,
            profileImageUrl
        );
    }
}