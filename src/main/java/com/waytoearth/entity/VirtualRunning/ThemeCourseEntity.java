package com.waytoearth.entity.VirtualRunning;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "theme_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "운영자 제공 테마 코스 엔티티")
public class ThemeCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "코스 ID", example = "1")
    private Long id;

    @Schema(description = "코스 제목", example = "서울 → 부산")
    private String title;

    @Schema(description = "코스 설명", example = "국내 대표 장거리 러닝 코스")
    private String description;

    @Schema(description = "총 거리 (km)", example = "350.5")
    private Double totalDistanceKm;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "themeCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSegmentEntity> segments;
}
