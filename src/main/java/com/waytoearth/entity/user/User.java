package com.waytoearth.entity.user;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.enums.AgeGroup;
import com.waytoearth.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "kakao_id", unique = true, nullable = false)
    private Long kakaoId;

    @Setter
    @Column(name = "nickname", length = 20, unique = true)
    private String nickname;

    @Setter
    @Column(name = "residence", length = 100)
    private String residence;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Setter
    @Column(name = "weekly_goal_distance", precision = 5, scale = 2)
    private BigDecimal weeklyGoalDistance;

    @Setter
    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    // S3 삭제를 위해 Key도 보관
    @Setter
    @Column(name = "profile_image_key", length = 1024)
    private String profileImageKey;

    @Column(name = "is_onboarding_completed", nullable = false)
    @Builder.Default
    private Boolean isOnboardingCompleted = false;

    // 자동 계산 통계 필드들
    @Column(name = "total_distance", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalDistance = BigDecimal.ZERO;

    @Column(name = "total_running_count")
    @Builder.Default
    private Integer totalRunningCount = 0;

    @Version
    private Long version;


    // 온보딩 완료 메서드
    public void completeOnboarding(String nickname, String residence, AgeGroup ageGroup,
                                   Gender gender, BigDecimal weeklyGoalDistance, String profileImageUrl) {
        this.nickname = nickname;
        this.residence = residence;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.weeklyGoalDistance = weeklyGoalDistance;
        this.profileImageUrl = profileImageUrl;
        this.isOnboardingCompleted = true;
    }

    // 러닝 통계 업데이트 메서드
    public void updateRunningStats(BigDecimal distance) {
        this.totalDistance = this.totalDistance.add(distance);
        this.totalRunningCount++;
    }


}