package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
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
public interface CrewRepository extends JpaRepository<CrewEntity, Long> {

    List<CrewEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    Optional<CrewEntity> findByIdAndIsActiveTrue(Long id);

    List<CrewEntity> findByOwnerAndIsActiveTrue(User owner);

    List<CrewEntity> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);

    @Query("SELECT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "WHERE c.id = :crewId AND c.isActive = true")
    Optional<CrewEntity> findByIdWithOwner(@Param("crewId") Long crewId);

    //사용자가 속한 크루 조회
    @Query("SELECT c FROM CrewEntity c " +
           "JOIN c.members m " +
           "WHERE m.user = :user AND m.isActive = true AND c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<CrewEntity> findCrewsByUser(@Param("user") User user);

    //가입 가능한 크루 조회
    @Query("SELECT c FROM CrewEntity c " +
           "WHERE c.isActive = true " +
           "AND c.id NOT IN (" +
           "  SELECT cm.crew.id FROM CrewMemberEntity cm " +
           "  WHERE cm.user = :user AND cm.isActive = true" +
           ") " +
           "AND c.id NOT IN (" +
           "  SELECT jr.crew.id FROM CrewJoinRequestEntity jr " +
           "  WHERE jr.user = :user AND jr.status = 'PENDING'" +
           ") " +
           "ORDER BY c.createdAt DESC")
    List<CrewEntity> findJoinableCrews(@Param("user") User user);

    @Query("SELECT COUNT(m) < c.maxMembers FROM CrewEntity c " +
           "LEFT JOIN c.members m ON m.isActive = true " +
           "WHERE c.id = :crewId AND c.isActive = true " +
           "GROUP BY c.id, c.maxMembers")
    boolean isCrewJoinable(@Param("crewId") Long crewId);

    @Query("SELECT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "WHERE c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<CrewEntity> findActiveCrewsWithOwner();

    //크루 이름으로 검색 (활성 크루만) - N+1 방지
    @Query("SELECT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "WHERE c.name LIKE %:keyword% AND c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<CrewEntity> searchByNameKeywordWithOwner(@Param("keyword") String keyword);

    //사용자가 속한 크루 조회 - N+1 방지
    @Query("SELECT DISTINCT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "JOIN c.members m " +
           "WHERE m.user = :user AND m.isActive = true AND c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<CrewEntity> findCrewsByUserWithOwner(@Param("user") User user);

    /**
     * 페이징 지원 메서드들
     */
    @Query("SELECT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "WHERE c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    Page<CrewEntity> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    //페이징 지원 크루 검색 - N+1 방지
    @Query("SELECT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "WHERE c.name LIKE %:keyword% AND c.isActive = true")
    Page<CrewEntity> findByNameContainingWithOwner(@Param("keyword") String keyword, Pageable pageable);

    //페이징 지원 사용자 크루 조회 - N+1 방지
    @Query("SELECT DISTINCT c FROM CrewEntity c " +
           "JOIN FETCH c.owner " +
           "JOIN c.members m " +
           "WHERE m.user = :user AND m.isActive = true AND c.isActive = true")
    Page<CrewEntity> findCrewsByUserWithOwnerPaged(@Param("user") User user, Pageable pageable);

    // Concurrency control: lock crew row for updates within a transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CrewEntity c WHERE c.id = :crewId")
    Optional<CrewEntity> findByIdForUpdate(@Param("crewId") Long crewId);
}
