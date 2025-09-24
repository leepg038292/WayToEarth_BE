package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "crew_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 월간 통계 엔티티")
public class CrewStatisticsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "통계 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Schema(description = "크루")
    private CrewEntity crew;

    @Schema(description = "통계 년월", example = "202412")
    @Column(nullable = false, length = 6)
    private String month;

    @Schema(description = "총 누적 거리 (km)", example = "456.8")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDistance = BigDecimal.ZERO;

    @Schema(description = "해당 월 활동한 고유 멤버 수", example = "12")
    @Column(nullable = false)
    @Builder.Default
    private Integer monthlyActiveMembers = 0;

    @Schema(description = "평균 페이스 (초)", example = "375")
    @Column(precision = 10, scale = 2)
    private BigDecimal avgPaceSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mvp_user_id")
    @Schema(description = "MVP 사용자")
    private User mvpUser;

    @Schema(description = "MVP 거리 (km)", example = "85.2")
    @Column(precision = 10, scale = 2)
    private BigDecimal mvpDistance;

    @Schema(description = "총 러닝 횟수", example = "156")
    @Column(nullable = false)
    @Builder.Default
    private Integer totalRuns = 0;

    public String getFormattedAvgPace() {
        if (avgPaceSeconds == null) {
            return "00:00";
        }
        int minutes = avgPaceSeconds.intValue() / 60;
        int seconds = avgPaceSeconds.intValue() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static CrewStatisticsEntity createMonthlyStats(CrewEntity crew, String month) {
        return CrewStatisticsEntity.builder()
                .crew(crew)
                .month(month)
                .totalDistance(BigDecimal.ZERO)
                .monthlyActiveMembers(0)
                .totalRuns(0)
                .build();
    }
}