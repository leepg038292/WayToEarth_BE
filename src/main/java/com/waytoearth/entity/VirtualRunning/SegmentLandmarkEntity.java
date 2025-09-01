package com.waytoearth.entity.VirtualRunning;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "segment_landmark")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "세그먼트 랜드마크 엔티티")
public class SegmentLandmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "랜드마크 ID", example = "501")
    private Long id;

    @Schema(description = "세그먼트 ID", example = "100")
    private Long segmentId;

    @Schema(description = "랜드마크 이름", example = "남산타워")
    private String name;

    @Schema(description = "위도", example = "37.5512")
    private Double latitude;

    @Schema(description = "경도", example = "126.9882")
    private Double longitude;

    @Schema(description = "사진 URL", example = "https://example.com/namsan.jpg")
    private String photoUrl;

    @Schema(description = "설명", example = "서울의 대표적인 전망 명소")
    private String description;
}
