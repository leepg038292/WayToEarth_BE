package com.waytoearth.dto.response.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@Schema(name = "UserInfoResponse", description = "내 정보 상세")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserInfoResponse {

    @Schema(description = "사용자 ID", example = "12345")
    private Long id;

    @Schema(description = "닉네임", example = "홍러너")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "거주지", example = "서울특별시 강남구")
    private String residence;

    @Schema(description = "연령대", example = "20대")
    private String ageGroup;

    @Schema(description = "성별", example = "남성")
    private String gender;

    @Schema(description = "주간 목표 거리(km)", example = "15.5")
    private BigDecimal weeklyGoalDistance;

    @Schema(description = "총 누적 거리(km)", example = "247.8")
    private BigDecimal totalDistance;

    @Schema(description = "총 러닝 횟수", example = "45")
    private Integer totalRunningCount;

    @Schema(description = "가입 일시(UTC)", example = "2025-01-15T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "프로필 이미지 Key (S3 오브젝트 키)", example = "profiles/1/profile.png")
    private String profileImageKey;

    public UserInfoResponse() { }

    public UserInfoResponse(Long id, String nickname, String profileImageUrl, String residence,
                            String ageGroup, String gender, BigDecimal weeklyGoalDistance,
                            BigDecimal totalDistance, Integer totalRunningCount, Instant createdAt, String profileImageKey) {
        this.id = id;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.residence = residence;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.weeklyGoalDistance = weeklyGoalDistance;
        this.totalDistance = totalDistance;
        this.totalRunningCount = totalRunningCount;
        this.createdAt = createdAt;
        this.profileImageKey = profileImageKey; //추가
    }

}
