package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.enums.CrewRole;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 멤버 엔티티")
public class CrewMemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "크루 멤버 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Schema(description = "크루")
    private CrewEntity crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "사용자")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "크루 내 역할", example = "MEMBER")
    private CrewRole role;

    @Schema(description = "가입일", example = "2024-01-15T10:30:00")
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Schema(description = "활성화 상태", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public boolean isOwner() {
        return role == CrewRole.OWNER;
    }

    public boolean isMember() {
        return role == CrewRole.MEMBER;
    }

    public static CrewMemberEntity createMember(CrewEntity crew, User user) {
        return CrewMemberEntity.builder()
                .crew(crew)
                .user(user)
                .role(CrewRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static CrewMemberEntity createOwner(CrewEntity crew, User user) {
        return CrewMemberEntity.builder()
                .crew(crew)
                .user(user)
                .role(CrewRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }
}