package com.waytoearth.entity.VirtualRunning;

import com.waytoearth.entity.enums.VirtualCourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_virtual_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 가상 코스 진행 상태 엔티티")
public class UserVirtualCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "사용자 코스 진행 ID", example = "200")
    private Long id;

    @Schema(description = "사용자 ID", example = "42")
    private Long userId;

    @Schema(description = "코스 ID", example = "10")
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Schema(description = "코스 타입 (CUSTOM, THEME)", example = "CUSTOM")
    private CourseType courseType;   // ✅ 추가

    @Schema(description = "진행률 (%)", example = "35.7")
    private Double progressPercent;

    @Schema(description = "누적 거리 (km)", example = "123.4")
    private Double totalDistanceAccumulated;

    @Enumerated(EnumType.STRING)
    @Schema(description = "진행 상태", example = "ACTIVE")
    private VirtualCourseStatus status;

    // ✅ Enum 정의 (내부 또는 별도 파일로 관리 가능)
    public enum CourseType {
        CUSTOM, THEME
    }
}
