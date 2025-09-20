package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stamps",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_journey_progress_id", "landmark_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스탬프 엔티티")
public class StampEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "스탬프 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_journey_progress_id", nullable = false)
    @Schema(description = "사용자 여행 진행")
    private UserJourneyProgressEntity userJourneyProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id", nullable = false)
    @Schema(description = "랜드마크")
    private LandmarkEntity landmark;

    @Schema(description = "수집 시간", example = "2024-01-15T14:30:00")
    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @Schema(description = "스탬프 이미지 URL", example = "https://example.com/stamp.png")
    private String stampImageUrl;

    @Schema(description = "특별 스탬프 여부", example = "false")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSpecial = false;

    @Schema(description = "스탬프 등급", example = "GOLD")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StampGrade grade = StampGrade.BRONZE;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (collectedAt == null) {
            collectedAt = LocalDateTime.now();
        }
    }

    public enum StampGrade {
        BRONZE,     // 일반 스탬프
        SILVER,     // 희귀 스탬프
        GOLD,       // 에픽 스탬프
        DIAMOND     // 전설 스탬프
    }
}