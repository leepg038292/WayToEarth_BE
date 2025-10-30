package com.waytoearth.entity.running;

import com.waytoearth.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "running_route",
        indexes = {
                @Index(name = "idx_route_record_seq", columnList = "running_record_id,sequence")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningRoute extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 러닝 기록 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_record_id", nullable = false)
    private RunningRecord runningRecord;

    /** 위도 */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** 경로 순서 */
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    // ========== 워치 연동 차트용 필드 ==========

    /** 러닝 시작 후 경과 시간(초) */
    @Column(name = "timestamp_seconds")
    private Integer timestampSeconds;

    /** 이 지점에서의 심박수(bpm) */
    @Column(name = "heart_rate")
    private Integer heartRate;

    /** 이 구간의 페이스(초/km) */
    @Column(name = "pace_seconds")
    private Integer paceSeconds;

    /** 고도(미터) */
    @Column(name = "altitude")
    private Double altitude;

    /** GPS 정확도(미터) */
    @Column(name = "accuracy")
    private Double accuracy;

    /** 이 지점까지의 누적 거리(미터) */
    @Column(name = "cumulative_distance_meters")
    private Integer cumulativeDistanceMeters;
}
