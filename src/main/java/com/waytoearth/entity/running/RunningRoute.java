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
}
