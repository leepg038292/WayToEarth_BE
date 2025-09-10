package com.waytoearth.entity.VirtualRunning;

import com.waytoearth.entity.enums.VirtualCourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "segment_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자의 세그먼트별 진행률 엔티티")
public class SegmentProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "세그먼트 진행 ID", example = "300")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_virtual_course_id")
    private UserVirtualCourseEntity userVirtualCourse;

    @Schema(description = "세그먼트 ID", example = "100")
    private Long segmentId;

    @Schema(description = "세그먼트 누적 거리 (km)", example = "15.2")
    private Double distanceAccumulated;

    @Enumerated(EnumType.STRING)
    @Schema(description = "진행 상태", example = "ACTIVE")
    private VirtualCourseStatus status;

    @Version
    @Schema(description = "낙관적 락 버전", hidden = true)
    private Long version;

}
