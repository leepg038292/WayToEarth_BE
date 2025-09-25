package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewMemberEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 멤버 정보 응답")
public class CrewMemberResponse {

    @Schema(description = "멤버십 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "123")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "김러너")
    private String userNickname;

    @Schema(description = "사용자 프로필 이미지", example = "https://example.com/profile.jpg")
    private String userProfileImage;

    @Schema(description = "크루 내 역할", example = "MEMBER")
    private String role;

    @Schema(description = "가입일", example = "2024-01-15T10:30:00")
    private LocalDateTime joinedAt;

    @Schema(description = "활성화 상태", example = "true")
    private Boolean isActive;

    @Schema(description = "크루장 여부", example = "false")
    private Boolean isOwner;

    public static CrewMemberResponse from(CrewMemberEntity member) {
        return new CrewMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getRole().name(),
                member.getJoinedAt(),
                member.getIsActive(),
                member.isOwner()
        );
    }
}