package com.waytoearth.entity.journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_card_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 카드 갤러리 이미지")
public class StoryCardImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "이미지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_card_id", nullable = false)
    private StoryCardEntity storyCard;

    @Schema(description = "이미지 URL", example = "https://cdn.waytoearth.com/journeys/1/landmarks/5/stories/10/uuid.jpg")
    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Schema(description = "정렬 순서", example = "0")
    @Column(nullable = false)
    private Integer orderIndex;
}

