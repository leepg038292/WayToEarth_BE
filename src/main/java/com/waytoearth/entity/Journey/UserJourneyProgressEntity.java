package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_journey_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 여행 진행 엔티티")
public class UserJourneyProgressEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "진행 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    @Schema(description = "여행")
    private JourneyEntity journey;

    @Schema(description = "현재 누적 거리 (km)", example = "123.4")
    @Column(nullable = false)
    @Builder.Default
    private Double currentDistanceKm = 0.0;

    @Schema(description = "진행률 (%)", example = "35.2")
    @Column(nullable = false)
    @Builder.Default
    private Double progressPercent = 0.0;

    @Schema(description = "진행 상태", example = "ACTIVE")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.ACTIVE;

    @Schema(description = "시작 시간", example = "2024-01-01T10:00:00")
    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Schema(description = "완료 시간", example = "2024-02-01T15:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "현재 러닝 세션 ID", example = "session-uuid-123")
    private String sessionId;

    @OneToMany(mappedBy = "userJourneyProgress", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StampEntity> collectedStamps = new ArrayList<>();


    @Version
    @Schema(description = "낙관적 락 버전", hidden = true)
    private Long version;

    public enum ProgressStatus {
        ACTIVE,     // 진행 중
        COMPLETED,  // 완료
        PAUSED      // 일시정지
    }

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }

    public void updateProgress(Double distanceKm) {
        this.currentDistanceKm += distanceKm;
        this.progressPercent = (this.currentDistanceKm / this.journey.getTotalDistanceKm()) * 100.0;

        if (this.progressPercent >= 100.0) {
            this.status = ProgressStatus.COMPLETED;
            this.completedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }
}