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

    // 세션 단건
    Optional<RunningRecord> findBySessionId(String sessionId);
    Optional<RunningRecord> findByIdAndUser(Long id, User user);

    // 기본 목록(완료된 기록) - 최신순
    Page<RunningRecord> findByUserAndIsCompletedTrueOrderByStartedAtDesc(User user, Pageable pageable);

    // 타입 필터 + 최신순
    Page<RunningRecord> findByUserAndRunningTypeAndIsCompletedTrueOrderByStartedAtDesc(
            User user, RunningType runningType, Pageable pageable);

    // 진행 중인 세션
    Optional<RunningRecord> findByUserAndIsCompletedFalse(User user);

    // 누적 개수/거리
    long countByUserAndIsCompletedTrue(User user);

    @Query("SELECT COALESCE(SUM(r.distance), 0) " +
            "FROM RunningRecord r " +
            "WHERE r.user = :user AND r.isCompleted = true")
    BigDecimal getTotalDistanceByUser(@Param("user") User user);

    //  범용 기간 조회 (서비스에서 주/월 계산 후 주입)
    List<RunningRecord> findCompletedByUserAndStartedAtBetweenOrderByStartedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    //  타입 + 기간 조회
    List<RunningRecord> findCompletedByUserAndRunningTypeAndStartedAtBetweenOrderByStartedAtDesc(
            User user, RunningType runningType, LocalDateTime startDate, LocalDateTime endDate);
}
