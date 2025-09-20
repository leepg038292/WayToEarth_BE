package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.enums.StoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 카드 엔티티")
public class StoryCardEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "스토리 카드 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id", nullable = false)
    @Schema(description = "랜드마크 엔티티")
    private LandmarkEntity landmark;

    @Schema(description = "스토리 제목", example = "경복궁의 역사")
    @Column(nullable = false, length = 100)
    private String title;

    @Schema(description = "스토리 내용", example = "경복궁은 1395년 태조 이성계에 의해 창건된...")
    @Column(nullable = false, length = 2000)
    private String content;

    @Schema(description = "스토리 이미지 URL", example = "https://example.com/story.jpg")
    private String imageUrl;

    @Schema(description = "스토리 타입", example = "HISTORY")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryType type;

    @Schema(description = "표시 순서", example = "1")
    @Column(nullable = false)
    private Integer orderIndex;

}