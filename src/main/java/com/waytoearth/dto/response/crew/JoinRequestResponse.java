package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 가입 신청 응답")
public class JoinRequestResponse {

    @Schema(description = "가입 신청 ID", example = "1")
    private Long id;

    @Schema(description = "크루 ID", example = "1")
    private Long crewId;

    @Schema(description = "크루 이름", example = "서울 러닝 크루")
    private String crewName;

    @Schema(description = "신청자 ID", example = "123")
    private Long userId;

    @Schema(description = "신청자 닉네임", example = "김러너")
    private String userNickname;

    @Schema(description = "신청자 프로필 이미지 URL", example = "https://cdn.waytoearth.com/profiles/123/profile.jpg")
    private String userProfileImageUrl;

    @Schema(description = "신청 메시지", example = "안녕하세요! 함께 러닝하고 싶습니다.")
    private String message;

    @Schema(description = "신청 상태", example = "PENDING")
    private String status;

    @Schema(description = "신청일", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "처리일", example = "2024-01-15T11:30:00")
    private LocalDateTime processedAt;

    @Schema(description = "처리자 닉네임", example = "크루장")
    private String processedByNickname;

    @Schema(description = "처리 메모", example = "환영합니다!")
    private String processingNote;

    public static JoinRequestResponse from(CrewJoinRequestEntity request, String userProfileImageUrl) {
        return new JoinRequestResponse(
                request.getId(),
                request.getCrew().getId(),
                request.getCrew().getName(),
                request.getUser().getId(),
                request.getUser().getNickname(),
                userProfileImageUrl,
                request.getMessage(),
                request.getStatus().name(),
                request.getCreatedAt(),
                request.getProcessedAt(),
                request.getProcessedBy() != null ? request.getProcessedBy().getNickname() : null,
                request.getProcessingNote()
        );
    }
}