package com.waytoearth.entity.journey;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "journey_routes",
    indexes = {
        @Index(name = "idx_journey_route_sequence", columnList = "journey_id,sequence")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_journey_sequence", columnNames = {"journey_id", "sequence"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여정 경로 엔티티")
public class JourneyRouteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "경로 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    @Schema(description = "소속 여정")
    @JsonBackReference
    private JourneyEntity journey;

    @Schema(description = "위도", example = "37.5665")
    @Column(name = "latitude", nullable = false)
    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    @Column(name = "longitude", nullable = false)
    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private Double longitude;

    @Schema(description = "경로 순서", example = "1")
    @Column(name = "sequence", nullable = false)
    @NotNull(message = "경로 순서는 필수입니다")
    @Min(value = 1, message = "경로 순서는 1 이상이어야 합니다")
    private Integer sequence;

    @Schema(description = "고도 (미터)", example = "120.5")
    @Column(name = "altitude")
    private Double altitude;

    @Schema(description = "구간 설명", example = "한강대교 진입")
    @Column(name = "description", length = 200)
    private String description;
}