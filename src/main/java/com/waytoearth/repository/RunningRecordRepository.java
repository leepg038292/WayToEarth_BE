package com.waytoearth.repository;

import com.waytoearth.entity.RunningRecord;
import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.RunningType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunningRecordRepository extends JpaRepository<RunningRecord, Long> {

    // 세션 ID로 조회 (진행 중인 세션 관리)
    Optional<RunningRecord> findBySessionId(String sessionId);

    // 사용자와 ID로 조회 (권한 체크 포함)
    Optional<RunningRecord> findByIdAndUser(Long id, User user);

    // 사용자의 완료된 러닝 기록 페이징 조회
    Page<RunningRecord> findByUserAndIsCompletedTrueOrderByStartedAtDesc(User user, Pageable pageable);

    // 사용자의 특정 타입 러닝 기록 페이징 조회
    Page<RunningRecord> findByUserAndRunningTypeAndIsCompletedTrueOrderByStartedAtDesc(
            User user, RunningType runningType, Pageable pageable);

    // 사용자의 전체 러닝 기록 개수
    long countByUserAndIsCompletedTrue(User user);

    // 사용자의 총 누적 거리 (완료된 기록 기준)
    @Query("SELECT COALESCE(SUM(r.distance), 0) FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true")
    BigDecimal getTotalDistanceByUser(@Param("user") User user);

    // 특정 기간 동안의 러닝 기록 조회
    @Query("SELECT r FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true " +
            "AND r.startedAt BETWEEN :startDate AND :endDate ORDER BY r.startedAt DESC")
    List<RunningRecord> findByUserAndPeriod(@Param("user") User user,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // 주간 통계
    @Query("SELECT r FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true " +
            "AND r.startedAt >= :weekStart ORDER BY r.startedAt")
    List<RunningRecord> findWeeklyRecords(@Param("user") User user,
                                          @Param("weekStart") LocalDateTime weekStart);

    // 월간 통계
    @Query("SELECT r FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true " +
            "AND YEAR(r.startedAt) = :year AND MONTH(r.startedAt) = :month ORDER BY r.startedAt")
    List<RunningRecord> findMonthlyRecords(@Param("user") User user,
                                           @Param("year") int year,
                                           @Param("month") int month);

    // 진행 중인 러닝 세션 조회
    Optional<RunningRecord> findByUserAndIsCompletedFalse(User user);
}
