package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.service.file.FileService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 목록 응답")
public class CrewListResponse {

    @Schema(description = "크루 ID", example = "1")
    private Long id;

    @Schema(description = "크루 이름", example = "서울 러닝 크루")
    private String name;

    @Schema(description = "크루 소개", example = "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다")
    private String description;

    @Schema(description = "최대 인원", example = "20")
    private Integer maxMembers;

    @Schema(description = "현재 멤버 수", example = "10")
    private Integer currentMembers;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;

    @Schema(description = "크루장 닉네임", example = "김러너")
    private String ownerNickname;

    @Schema(description = "생성일 (UTC)", example = "2024-01-15T01:30:00Z")
    private Instant createdAt;

    @Schema(description = "가입 가능 여부", example = "true")
    private Boolean canJoin;

    public static CrewListResponse from(CrewEntity crew, Boolean canJoin, FileService fileService) {
        // profileImageKey가 있으면 CloudFront URL 생성, 없으면 기존 profileImageUrl 사용
        String profileImageUrl = null;
        if (crew.getProfileImageKey() != null && !crew.getProfileImageKey().isEmpty()) {
            profileImageUrl = fileService.createPresignedGetUrl(crew.getProfileImageKey());
        } else if (crew.getProfileImageUrl() != null) {
            // 기존 데이터 호환성 (key가 없는 경우)
            profileImageUrl = crew.getProfileImageUrl();
        }

        return new CrewListResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getMaxMembers(),
                crew.getCurrentMembers(),
                profileImageUrl,
                crew.getOwner().getNickname(),
                crew.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                canJoin
        );
    }

    // 기존 메서드 유지 (하위 호환성)
    @Deprecated
    public static CrewListResponse from(CrewEntity crew, Boolean canJoin) {
        return new CrewListResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getMaxMembers(),
                crew.getCurrentMembers(),
                crew.getProfileImageUrl(),
                crew.getOwner().getNickname(),
                crew.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                canJoin
        );
    }

    @Deprecated
    public static CrewListResponse from(CrewEntity crew) {
        return from(crew, null);
    }
}