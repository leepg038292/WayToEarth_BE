package com.waytoearth.entity.emblem;

import com.waytoearth.entity.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "emblems")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "엠블럼 정보 엔티티")
public class Emblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "엠블럼 ID", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "엠블럼 이름", example = "첫 러닝 완주")
    private String name;

    @Column(length = 500)
    @Schema(description = "엠블럼 설명", example = "첫 러닝을 완주하면 지급됩니다.")
    private String description;

    @Column(length = 500)
    @Schema(description = "엠블럼 이미지 URL", example = "https://cdn.example.com/emblems/1.png")
    private String imageUrl;

    @Column(length = 20)
    @Schema(description = "희귀도", example = "COMMON")
    private String rarity; // OPTIONAL

    @Column(name = "condition_type", length = 30)
    @Schema(description = "지급 조건 타입", example = "DISTANCE")
    private String conditionType; // OPTIONAL (서비스에서 사용)

    @Column(name = "condition_value", precision = 8, scale = 2)
    @Schema(description = "지급 조건 값", example = "100.00")
    private BigDecimal conditionValue; // OPTIONAL (서비스에서 사용)

}
