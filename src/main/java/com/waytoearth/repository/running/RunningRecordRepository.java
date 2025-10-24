package com.waytoearth.repository.running;

import com.waytoearth.entity.running.RunningRecord;
import com.waytoearth.entity.user.User;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.repository.statistics.StatisticsRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunningRecordRepository extends JpaRepository<RunningRecord, Long>, StatisticsRepositoryCustom {

    // 진행 중 세션 존재 여부(중복 시작 방지)
    boolean existsBySessionIdAndIsCompletedFalse(String sessionId);

    // ===== 단건 조회 =====
    Optional<RunningRecord> findBySessionId(String sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RunningRecord r WHERE r.sessionId = :sessionId")
    Optional<RunningRecord> findBySessionIdWithLock(@Param("sessionId") String sessionId);

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

    // 사용자 완료 기록 최신순 (기록 탭 카드)
    List<RunningRecord> findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(Long userId);

    //  상세 조회 시 경로(routes) 즉시 로드
    @EntityGraph(attributePaths = "routes")
    Optional<RunningRecord> findWithRoutesById(Long id);

    // ===== 커서 기반 페이징 =====
    // 첫 페이지: 최신 데이터부터
    @Query("SELECT r FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true " +
           "ORDER BY r.id DESC")
    List<RunningRecord> findTopNByUserOrderByIdDesc(@Param("user") User user, Pageable pageable);

    // 다음 페이지: cursor 이후 데이터
    @Query("SELECT r FROM RunningRecord r WHERE r.user = :user AND r.isCompleted = true " +
           "AND r.id < :cursor ORDER BY r.id DESC")
    List<RunningRecord> findNextPageByUserAndCursor(
            @Param("user") User user,
            @Param("cursor") Long cursor,
            Pageable pageable);

    // 사용자 ID로 러닝 기록 일괄 삭제 (회원 탈퇴용)
    void deleteByUserId(Long userId);

    // 사용자의 최근 러닝 날짜 조회 (완료된 러닝 중 가장 최근 started_at)
    @Query("""
           SELECT r.startedAt
           FROM RunningRecord r
           WHERE r.user.id = :userId AND r.isCompleted = true
           ORDER BY r.startedAt DESC
           LIMIT 1
           """)
    Optional<LocalDateTime> findLatestRunningDateByUserId(@Param("userId") Long userId);

    // 여러 사용자의 최근 러닝 날짜를 배치 조회 (N+1 문제 해결)
    @Query("""
           SELECT r.user.id, MAX(r.startedAt)
           FROM RunningRecord r
           WHERE r.user.id IN :userIds AND r.isCompleted = true
           GROUP BY r.user.id
           """)
    List<Object[]> findLatestRunningDatesByUserIds(@Param("userIds") List<Long> userIds);
}