package com.waytoearth.entity.emblem;

import com.waytoearth.entity.User.User;
import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(
        name = "user_emblems",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_emblem", columnNames = {"user_id", "emblem_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "사용자-엠블럼 수집 내역 엔티티")
public class UserEmblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "유저-엠블럼 관계 ID", example = "10")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "엠블럼을 보유한 사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emblem_id", nullable = false)
    @Schema(description = "보유한 엠블럼")
    private Emblem emblem;

    @Column(name = "acquired_at")
    @Schema(description = "엠블럼 획득 시각", example = "2024-01-15T10:30:00Z")
    private Instant acquiredAt;

}
