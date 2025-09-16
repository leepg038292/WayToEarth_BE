package com.waytoearth.entity.VirtualRunning;

import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "custom_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정의 커스텀 코스 엔티티")
public class CustomCourseEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "커스텀 코스 ID", example = "10")
    private Long id;

    @Schema(description = "사용자 ID", example = "42")
    private Long userId;

    @Schema(description = "코스 제목", example = "한강 러닝 코스")
    private String title;

    @Schema(description = "총 거리 (km)", example = "12.3")
    private Double totalDistanceKm;


    @OneToMany(mappedBy = "customCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSegmentEntity> segments;

}
