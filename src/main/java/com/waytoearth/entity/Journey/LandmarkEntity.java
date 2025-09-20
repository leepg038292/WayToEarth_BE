package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "landmarks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "랜드마크 엔티티")
public class LandmarkEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "랜드마크 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    @Schema(description = "여행 엔티티")
    private JourneyEntity journey;

    @Schema(description = "랜드마크 이름", example = "경복궁")
    @Column(nullable = false, length = 100)
    private String name;

    @Schema(description = "설명", example = "조선왕조의 법궁으로 600년 역사를 자랑하는 궁궐")
    @Column(length = 1000)
    private String description;

    @Schema(description = "위도", example = "37.5796")
    @Column(nullable = false)
    private Double latitude;

    @Schema(description = "경도", example = "126.9770")
    @Column(nullable = false)
    private Double longitude;

    @Schema(description = "시작점으로부터 거리 (km)", example = "25.5")
    @Column(nullable = false)
    private Double distanceFromStart;

    @Schema(description = "순서", example = "1")
    @Column(nullable = false)
    private Integer orderIndex;

    @Schema(description = "랜드마크 이미지 URL", example = "https://example.com/landmark.jpg")
    private String imageUrl;

    @Schema(description = "국가 코드", example = "KR")
    @Column(length = 2)
    private String countryCode;

    @Schema(description = "도시명", example = "서울")
    @Column(length = 50)
    private String cityName;

    @OneToMany(mappedBy = "landmark", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoryCardEntity> storyCards = new ArrayList<>();
}