package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crews",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"name"}, name = "uk_crew_name")
       })
@org.hibernate.annotations.Check(constraints = "max_members > 0 AND max_members <= 1000 AND current_members >= 0 AND current_members <= max_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 엔티티")
public class CrewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "크루 ID", example = "1")
    private Long id;

    @Schema(description = "크루 이름", example = "서울 러닝 크루")
    @Column(nullable = false, length = 50)
    private String name;

    @Schema(description = "크루 소개", example = "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다")
    @Column(length = 500)
    private String description;

    @Schema(description = "최대 인원", example = "20")
    @Column(nullable = false)
    @Builder.Default
    private Integer maxMembers = 50;

    @Schema(description = "프로필 이미지 URL (deprecated, CloudFront URL 사용 권장)", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;

    @Schema(description = "프로필 이미지 S3 Key", example = "crews/123/profile_1234567890.jpg")
    @Column(name = "profile_image_key", length = 512)
    private String profileImageKey;

    @Schema(description = "현재 멤버 수", example = "10")
    @Column(nullable = false)
    @Builder.Default
    private Integer currentMembers = 0;

    @Schema(description = "낙관적 잠금을 위한 버전 필드")
    @Version
    @Builder.Default
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @Schema(description = "크루장")
    private User owner;

    @OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "크루 멤버들")
    private List<CrewMemberEntity> members = new ArrayList<>();

    @OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "가입 신청들")
    private List<CrewJoinRequestEntity> joinRequests = new ArrayList<>();

    // 비즈니스 메서드
    public boolean isFull() {
        return currentMembers >= maxMembers;
    }

    public boolean isOwner(User user) {
        return owner.equals(user);
    }

    public int getCurrentMemberCount() {
        return currentMembers;
    }

    public void incrementMemberCount() {
        this.currentMembers++;
    }

    public void decrementMemberCount() {
        this.currentMembers--;
    }
}