package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity.JoinRequestStatus;
import com.waytoearth.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewJoinRequestRepository extends JpaRepository<CrewJoinRequestEntity, Long> {

    List<CrewJoinRequestEntity> findByCrewAndStatusOrderByCreatedAtAsc(CrewEntity crew, JoinRequestStatus status);

    List<CrewJoinRequestEntity> findByUserOrderByCreatedAtDesc(User user);

    Optional<CrewJoinRequestEntity> findByCrewAndUser(CrewEntity crew, User user);

    boolean existsByCrewAndUserAndStatus(CrewEntity crew, User user, JoinRequestStatus status);

    long countByCrewAndStatus(CrewEntity crew, JoinRequestStatus status);

    //대기중인 신청 목록 (사용자 정보 포함)
    @Query("SELECT jr FROM CrewJoinRequestEntity jr " +
           "JOIN FETCH jr.user " +
           "WHERE jr.crew = :crew AND jr.status = 'PENDING' " +
           "ORDER BY jr.createdAt ASC")
    List<CrewJoinRequestEntity> findPendingRequestsWithUser(@Param("crew") CrewEntity crew);

    //사용자의 특정 크루 신청 상태 조회 (가장 최근 1건만)
    @Query(value = "SELECT * FROM crew_join_requests " +
           "WHERE crew_id = :crewId AND user_id = :userId " +
           "ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<CrewJoinRequestEntity> findUserRequest(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //사용자가 대기중인 신청이 있는지 확인
    @Query("SELECT CASE WHEN COUNT(jr) > 0 THEN true ELSE false END " +
           "FROM CrewJoinRequestEntity jr " +
           "WHERE jr.crew.id = :crewId AND jr.user.id = :userId AND jr.status = 'PENDING'")
    boolean hasUserAlreadyRequested(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //크루장이 관리하는 모든 크루의 대기중인 신청 수
    @Query("SELECT COUNT(jr) FROM CrewJoinRequestEntity jr " +
           "WHERE jr.crew.owner.id = :ownerId AND jr.status = 'PENDING'")
    int countPendingRequestsForOwner(@Param("ownerId") Long ownerId);

    //크루 삭제 시 대기중인 모든 신청을 거절로 변경
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE CrewJoinRequestEntity jr SET jr.status = 'REJECTED' " +
           "WHERE jr.crew.id = :crewId AND jr.status = 'PENDING'")
    int rejectAllPendingRequests(@Param("crewId") Long crewId);

    //DB 페이징을 사용한 크루별 가입 신청 조회 (성능 최적화)
    @Query("SELECT jr FROM CrewJoinRequestEntity jr " +
           "JOIN FETCH jr.user " +
           "JOIN FETCH jr.crew " +
           "LEFT JOIN FETCH jr.processedBy " +
           "WHERE jr.crew.id = :crewId " +
           "AND (:status IS NULL OR jr.status = :status)")
    Page<CrewJoinRequestEntity> findCrewJoinRequestsWithPaging(
            @Param("crewId") Long crewId,
            @Param("status") JoinRequestStatus status,
            Pageable pageable);

    //사용자의 모든 신청 내역 (크루 정보 포함)
    @Query("SELECT jr FROM CrewJoinRequestEntity jr " +
           "JOIN FETCH jr.crew " +
           "WHERE jr.user = :user " +
           "ORDER BY jr.createdAt DESC")
    List<CrewJoinRequestEntity> findMyApplicationsWithCrew(@Param("user") User user);

    //특정 상태의 신청들 (처리자 정보 포함)
    @Query("SELECT jr FROM CrewJoinRequestEntity jr " +
           "LEFT JOIN FETCH jr.processedBy " +
           "WHERE jr.crew = :crew AND jr.status = :status " +
           "ORDER BY jr.processedAt DESC")
    List<CrewJoinRequestEntity> findByCrewAndStatusWithProcessor(@Param("crew") CrewEntity crew,
                                                                  @Param("status") JoinRequestStatus status);

    /**
     * 사용자 ID로 크루 가입 신청 일괄 삭제 (회원 탈퇴용)
     */
    void deleteByUserId(Long userId);

    // Concurrency control: lock join request row to prevent double-processing
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT jr FROM CrewJoinRequestEntity jr WHERE jr.id = :id")
    Optional<CrewJoinRequestEntity> findByIdForUpdate(@Param("id") Long id);
}
