package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crew_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 월간 러닝 횟수 기반 통계 엔티티")
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

    @Schema(description = "해당 월 뛴 러닝 횟수", example = "15")
    @Column(nullable = false)
    @Builder.Default
    private Integer runCount = 0;

    @Schema(description = "해당 월 뛴 총 거리 (km)", example = "456.8")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDistance = BigDecimal.ZERO;

    @Schema(description = "해당 월 참여한 고유 멤버 수", example = "12")
    @Column(nullable = false)
    @Builder.Default
    private Integer activeMembers = 0;

    @Schema(description = "해당 월 뛴 만큼의 평균 페이스 (초)", example = "375")
    @Column(precision = 10, scale = 2)
    private BigDecimal avgPaceSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mvp_user_id")
    @Schema(description = "해당 월 MVP 사용자 (거리 기준)")
    private User mvpUser;

    @Schema(description = "MVP의 해당 월 총 거리 (km)", example = "85.2")
    @Column(precision = 10, scale = 2)
    private BigDecimal mvpDistance;

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
                .runCount(0)
                .totalDistance(BigDecimal.ZERO)
                .activeMembers(0)
                .build();
    }

    /**
     * 해당 월 러닝 완료시 통계 업데이트
     */
    public void updateWithNewRun(BigDecimal newDistance, BigDecimal newPaceSeconds, int participantCount) {
        this.runCount++;
        this.totalDistance = this.totalDistance.add(newDistance);

        // 평균 페이스 재계산
        if (this.avgPaceSeconds == null) {
            this.avgPaceSeconds = newPaceSeconds;
        } else {
            // (기존평균 * (횟수-1) + 새페이스) / 횟수
            BigDecimal totalPaceSeconds = this.avgPaceSeconds.multiply(BigDecimal.valueOf(this.runCount - 1));
            this.avgPaceSeconds = totalPaceSeconds.add(newPaceSeconds).divide(BigDecimal.valueOf(this.runCount), 2, BigDecimal.ROUND_HALF_UP);
        }

        // 참여 멤버 수 업데이트 (최대값 유지)
        this.activeMembers = Math.max(this.activeMembers, participantCount);
    }

    /**
     * 새 달 시작시 통계 초기화 여부 확인
     */
    public boolean needsReset(String currentMonth) {
        return !this.month.equals(currentMonth);
    }

    /**
     * 새 달 통계로 리셋
     */
    public void resetForNewMonth(String newMonth) {
        this.month = newMonth;
        this.runCount = 0;
        this.totalDistance = BigDecimal.ZERO;
        this.activeMembers = 0;
        this.avgPaceSeconds = null;
        this.mvpUser = null;
        this.mvpDistance = null;
    }
}