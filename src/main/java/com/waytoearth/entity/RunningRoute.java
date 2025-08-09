package com.waytoearth.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 러닝 중 기록된 GPS 좌표 데이터
 * - 각 러닝 기록(RunningRecord)에 여러 좌표(RunningRoute)가 연결됨
 */
@Entity
@Table(name = "running_routes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RunningRoute {

    /** 경로 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long id;

    /** 소속된 러닝 기록 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_record_id", nullable = false)
    @ToString.Exclude // 무한 참조 방지
    private RunningRecord runningRecord;

    /** 위도 */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** 경로 순서 (0부터 시작 or 1부터 시작) */
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    /** 해당 좌표의 기록 시각 (Epoch millis) */
    @Column(name = "timestamp")
    private Long timestamp;
}
