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

@Entity
@Table(name = "running_records")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "running_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "title", length = 100)
    @Builder.Default
    private String title = "러닝 기록";

    @Enumerated(EnumType.STRING)
    @Column(name = "running_type", nullable = false)
    @Builder.Default
    private RunningType runningType = RunningType.SINGLE;

    @Column(name = "distance", precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(name = "duration")
    private Integer duration; // 초 단위

    @Column(name = "average_pace", length = 10)
    private String averagePace; // "05:47" 형식

    @Column(name = "calories")
    private Integer calories;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition")
    private WeatherCondition weatherCondition;

    @Column(name = "weather_temperature")
    private Integer temperature;

    @Column(name = "weather_humidity")
    private Integer humidity;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    // 경로 데이터를 위한 OneToMany 관계
    @OneToMany(mappedBy = "runningRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RunningRoute> routeData = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void complete(BigDecimal distance, Integer duration, String averagePace,
                         Integer calories, LocalDateTime endedAt) {
        this.distance = distance;
        this.duration = duration;
        this.averagePace = averagePace;
        this.calories = calories;
        this.endedAt = endedAt;
        this.isCompleted = true;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void addRoutePoint(Double latitude, Double longitude, Integer sequence) {
        RunningRoute route = RunningRoute.builder()
                .runningRecord(this)
                .latitude(latitude)
                .longitude(longitude)
                .sequence(sequence)
                .build();
        this.routeData.add(route);
    }

    // 평균 페이스 계산 메서드 (km당 분:초)
    public String calculateAveragePace() {
        if (distance == null || distance.compareTo(BigDecimal.ZERO) == 0 || duration == null) {
            return "00:00";
        }

        // 초당 킬로미터 -> km당 분:초
        double secondsPerKm = duration / distance.doubleValue();
        int minutes = (int) (secondsPerKm / 60);
        int seconds = (int) (secondsPerKm % 60);

        return String.format("%02d:%02d", minutes, seconds);
    }

    // 칼로리 계산 메서드 (간단한 추정)
    public Integer calculateCalories() {
        if (distance == null || duration == null) {
            return 0;
        }

        // 대략적인 계산: 1km당 60kcal (체중 70kg 기준)
        // 더 정확한 계산을 위해서는 사용자 체중, 나이, 성별 등이 필요
        return (int) (distance.doubleValue() * 60);
    }
}