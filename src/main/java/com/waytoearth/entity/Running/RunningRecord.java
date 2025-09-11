package com.waytoearth.entity.Running;

import com.waytoearth.entity.User.User;
import com.waytoearth.entity.enums.RunningStatus;
import com.waytoearth.entity.enums.RunningType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "running_record",
        indexes = {
                @Index(name = "idx_running_user_completed_started", columnList = "user_id,is_completed,started_at"),
                @Index(name = "idx_running_session", columnList = "session_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 러닝 소유 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 세션 ID (러닝 고유 식별) */
    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    /** 러닝 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "running_type", nullable = false, length = 20)
    private RunningType runningType;

    /** 가상 코스 ID (가상 러닝 시 사용) */
    @Column(name = "virtual_course_id")
    private Long virtualCourseId;

    /** 러닝 제목 */
    @Column(length = 100)
    private String title;

    /** 러닝 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RunningStatus status;

    /** 총 거리(km) */
    @Column(name = "distance", precision = 10, scale = 2)
    private BigDecimal distance;

    /** 총 시간(초) */
    @Column(name = "duration_seconds")
    private Integer duration;

    /** 평균 페이스(초/킬로미터) */
    @Column(name = "average_pace_seconds")
    private Integer averagePaceSeconds;

    /** 칼로리(kcal) */
    @Column(name = "calories")
    private Integer calories;

    /** 완료 여부 */
    @Column(name = "is_completed", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isCompleted;

    /** 시작 시각 */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** 종료 시각 */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** 경로 데이터 */
    @OneToMany(mappedBy = "runningRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<RunningRoute> routes = new ArrayList<>();

    /** 러닝 완료 처리 */
    public void complete(BigDecimal distanceKm, Integer durationSeconds, Integer paceSec,
                         Integer calories, LocalDateTime endedAt) {
        this.distance = distanceKm;
        this.duration = durationSeconds;
        this.averagePaceSeconds = paceSec;
        this.calories = calories;
        this.endedAt = endedAt;
        this.isCompleted = true;
        this.status = RunningStatus.COMPLETED;
    }

    /** 경로 포인트 추가 */
    public void addRoutePoint(Double latitude, Double longitude, Integer sequence) {
        RunningRoute route = new RunningRoute();
        route.setRunningRecord(this);
        route.setLatitude(latitude);
        route.setLongitude(longitude);
        route.setSequence(sequence);
        this.routes.add(route);
    }
}
