package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guestbook")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방명록 엔티티")
public class GuestbookEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "방명록 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id", nullable = false)
    @Schema(description = "랜드마크")
    private LandmarkEntity landmark;

    @Schema(description = "방명록 메시지", example = "정말 아름다운 곳이에요!")
    @Column(nullable = false, length = 500)
    private String message;

    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    private String photoUrl;

    @Schema(description = "기분", example = "AMAZED")
    @Enumerated(EnumType.STRING)
    private Mood mood;

    @Schema(description = "평점 (1-5)", example = "5")
    @Column(columnDefinition = "TINYINT CHECK (rating >= 1 AND rating <= 5)")
    private Integer rating;

    @Schema(description = "공개 여부", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    public enum Mood {
        HAPPY,      // 행복한
        EXCITED,    // 신난
        TIRED,      // 피곤한
        AMAZED      // 놀란
    }
}