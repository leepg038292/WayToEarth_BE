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
import org.springframework.data.jpa.repository.EntityGraph;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunningRecordRepository extends JpaRepository<RunningRecord, Long> {

    // 진행 중 세션 존재 여부(중복 시작 방지)
    boolean existsBySessionIdAndIsCompletedFalse(String sessionId);

    // ===== 단건 조회 =====
    Optional<RunningRecord> findBySessionId(String sessionId);
    Optional<RunningRecord> findByIdAndUser(Long id, User user);

    // 진행 중인 세션(사용자당 0~1개가 정상)
    Optional<RunningRecord> findByUserAndIsCompletedFalse(User user);
    boolean existsByUserAndIsCompletedFalse(User user);

    // ===== 목록 조회 (완료된 기록) =====
    // 기본 목록 - 최신순 페이징
    Page<RunningRecord> findByUserAndIsCompletedTrueOrderByStartedAtDesc(User user, Pageable pageable);

    // 타입 필터 + 최신순 페이징
    Page<RunningRecord> findByUserAndRunningTypeAndIsCompletedTrueOrderByStartedAtDesc(
            User user, RunningType runningType, Pageable pageable);

    // 기간 필터 (비페이징, 최신/오름 정렬 각각 제공)
    List<RunningRecord> findByUserAndIsCompletedTrueAndStartedAtBetweenOrderByStartedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    List<RunningRecord> findByUserAndRunningTypeAndIsCompletedTrueAndStartedAtBetweenOrderByStartedAtDesc(
            User user, RunningType runningType, LocalDateTime startDate, LocalDateTime endDate);

    List<RunningRecord> findByUserAndIsCompletedTrueAndStartedAtBetweenOrderByStartedAtAsc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    // 기간 필터 + 페이징 (필요 시 사용)
    Page<RunningRecord> findByUserAndIsCompletedTrueAndStartedAtBetweenOrderByStartedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<RunningRecord> findByUserAndRunningTypeAndIsCompletedTrueAndStartedAtBetweenOrderByStartedAtDesc(
            User user, RunningType runningType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ===== 누적/통계 =====
    long countByUserAndIsCompletedTrue(User user);

    @Query("""
           select coalesce(sum(r.distance), 0)
           from RunningRecord r
           where r.user = :user and r.isCompleted = true
           """)
    BigDecimal sumCompletedDistanceByUser(@Param("user") User user);

    // 사용자 완료 기록 최신순 (기록 탭 하단 카드)
    List<RunningRecord> findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(Long userId);

    //  상세 조회 시 경로(routes)까지 즉시 로드 (완료 페이지 재표시용)
    @EntityGraph(attributePaths = "routes")
    Optional<RunningRecord> findWithRoutesById(Long id);
}