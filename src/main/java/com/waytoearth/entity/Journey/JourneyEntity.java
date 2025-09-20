package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.enums.JourneyCategory;
import com.waytoearth.entity.enums.JourneyDifficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journeys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 엔티티")
public class JourneyEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "여행 ID", example = "1")
    private Long id;

    @Schema(description = "여행 제목", example = "서울에서 부산까지")
    @Column(nullable = false, length = 100)
    private String title;

    @Schema(description = "여행 설명", example = "한국의 대표적인 장거리 여행 코스")
    @Column(length = 500)
    private String description;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnailUrl;

    @Schema(description = "총 거리 (km)", example = "350.5")
    @Column(nullable = false)
    private Double totalDistanceKm;

    @Schema(description = "난이도", example = "MEDIUM")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourneyDifficulty difficulty;

    @Schema(description = "카테고리", example = "DOMESTIC")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourneyCategory category;

    @Schema(description = "예상 완주 기간 (일)", example = "30")
    private Integer estimatedDays;

    @Schema(description = "활성화 상태", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LandmarkEntity> landmarks = new ArrayList<>();
}