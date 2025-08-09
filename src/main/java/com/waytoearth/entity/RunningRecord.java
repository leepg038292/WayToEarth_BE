package com.waytoearth.entity;

import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.entity.enums.WeatherCondition;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 러닝 기록을 저장하는 엔티티
 * - 러닝 시작 시 sessionId로 생성
 * - 러닝 완료 시 거리, 시간, 평균 페이스, 칼로리 등 기록
 * - isCompleted 플래그를 통해 완료 여부 판단
 */
@Entity
@Table(name = "running_records")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RunningRecord {

    /** 러닝 기록 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "running_record_id")
    private Long id;

    /** 러닝을 수행한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 러닝 세션 식별자 (UUID 등) */
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    /** 러닝 제목 (기본값: "러닝 기록") */
    @Column(name = "title", length = 100)
    @Builder.Default
    private String title = "러닝 기록";

    /** 러닝 타입 (SINGLE, VIRTUAL 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "running_type", nullable = false)
    @Builder.Default
    private RunningType runningType = RunningType.SINGLE;

    /** 이동 거리 (km) */
    @Column(name = "distance", precision = 10, scale = 2)
    private BigDecimal distance;

    /** 소요 시간 (초 단위) */
    @Column(name = "duration")
    private Integer duration;

    /** 평균 페이스 (예: "05:47") */
    @Column(name = "average_pace", length = 10)
    private String averagePace;

    /** 추정 소모 칼로리 (단위: kcal) */
    @Column(name = "calories")
    private Integer calories;

    /** 날씨 정보 (맑음, 흐림, 비 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition")
    private WeatherCondition weatherCondition;

    /** 러닝 시작 시간 */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** 러닝 종료 시간 */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** 러닝 완료 여부 */
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    /** 경로 데이터 (위도, 경도, 순서 정보 포함) */
    @OneToMany(mappedBy = "runningRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RunningRoute> routeData = new ArrayList<>();

    /** 생성일 (자동 설정) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일 (자동 설정) */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================== 비즈니스 메서드 ==================

    /**
     * 러닝 완료 처리 메서드
     */
    public void complete(BigDecimal distance, Integer duration, String averagePace,
                         Integer calories, LocalDateTime endedAt) {
        this.distance = distance;
        this.duration = duration;
        this.averagePace = averagePace;
        this.calories = calories;
        this.endedAt = endedAt;
        this.isCompleted = true;
    }

    /**
     * 러닝 제목 수정
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 경로 좌표 추가
     */
    public void addRoutePoint(Double latitude, Double longitude, Integer sequence) {
        RunningRoute route = RunningRoute.builder()
                .runningRecord(this)
                .latitude(latitude)
                .longitude(longitude)
                .sequence(sequence)
                .build();
        this.routeData.add(route);
    }

    /**
     * 평균 페이스 계산 메서드
     * - km당 분:초 형식 ("05:47") 반환
     */
    public String calculateAveragePace() {
        if (distance == null || distance.compareTo(BigDecimal.ZERO) == 0 || duration == null) {
            return "00:00";
        }

        double secondsPerKm = duration / distance.doubleValue();
        int minutes = (int) (secondsPerKm / 60);
        int seconds = (int) (secondsPerKm % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 칼로리 계산 메서드 (단순 추정)
     * - 1km당 약 60kcal (체중 평균 기준)
     */
    public Integer calculateCalories() {
        if (distance == null || duration == null) {
            return 0;
        }
        return (int) (distance.doubleValue() * 60);
    }
}
