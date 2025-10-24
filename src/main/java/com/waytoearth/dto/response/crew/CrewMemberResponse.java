package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.service.file.FileService;
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

    @Schema(description = "최근 러닝 날짜 (없으면 null)", example = "2024-10-20T14:30:00")
    private LocalDateTime lastRunningDate;

    /**
     * CloudFront URL을 사용하는 정적 팩토리 메서드
     * @param member 크루 멤버 엔티티
     * @param fileService CloudFront URL 생성을 위한 파일 서비스
     * @return CrewMemberResponse
     */
    public static CrewMemberResponse from(CrewMemberEntity member, FileService fileService) {
        // profileImageKey로 CloudFront URL 생성
        String profileImageUrl = null;
        if (member.getUser().getProfileImageKey() != null &&
            !member.getUser().getProfileImageKey().isEmpty()) {
            profileImageUrl = fileService.createPresignedGetUrl(member.getUser().getProfileImageKey());
        }

        return new CrewMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                profileImageUrl,
                member.getRole().name(),
                member.getJoinedAt(),
                member.getIsActive(),
                member.isOwner()
        );
    }

    /**
     * 하위 호환성을 위한 메서드 (deprecated)
     * @deprecated FileService를 사용하는 from(CrewMemberEntity, FileService) 메서드를 사용하세요
     */
    @Deprecated
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