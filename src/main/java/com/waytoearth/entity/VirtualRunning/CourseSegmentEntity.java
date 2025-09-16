package com.waytoearth.entity.VirtualRunning;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.enums.SegmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_segment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "코스를 이루는 세그먼트 엔티티")
public class CourseSegmentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "세그먼트 ID", example = "100")
    private Long id;

    // ✅ ThemeCourse와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_course_id")
    private ThemeCourseEntity themeCourse;

    // ✅ CustomCourse와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_course_id")
    private CustomCourseEntity customCourse;

    @Enumerated(EnumType.STRING)
    @Schema(description = "세그먼트 타입", example = "DOMESTIC")
    private SegmentType type;

    @Schema(description = "세그먼트 순서", example = "1")
    private Integer orderIndex;

    @Schema(description = "시작 위도", example = "37.5665")
    private Double startLat;

    @Schema(description = "시작 경도", example = "126.9780")
    private Double startLng;

    @Schema(description = "종료 위도", example = "35.1796")
    private Double endLat;

    @Schema(description = "종료 경도", example = "129.0756")
    private Double endLng;

    @Schema(description = "세그먼트 거리 (km)", example = "45.7")
    private Double distanceKm;
}
