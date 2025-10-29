package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.enums.CrewRole;
import com.waytoearth.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long> {

    Optional<CrewMemberEntity> findByCrewAndRole(CrewEntity crew, CrewRole role);

    // userId로 크루 조회 (크루 정보 포함, 통계 업데이트용)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.crew " +
           "WHERE cm.user.id = :userId " +
           "ORDER BY cm.joinedAt DESC")
    List<CrewMemberEntity> findByUserIdWithCrew(@Param("userId") Long userId);

    long countByCrew(CrewEntity crew);

    //N+1 해결을 위한 fetch join
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.user " +
           "WHERE cm.crew = :crew " +
           "ORDER BY cm.joinedAt ASC")
    List<CrewMemberEntity> findByCrewWithUser(@Param("crew") CrewEntity crew);

    //사용자별 크루 조회 (크루 정보 포함)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.crew " +
           "WHERE cm.user = :user " +
           "ORDER BY cm.joinedAt DESC")
    List<CrewMemberEntity> findByUserWithCrew(@Param("user") User user);

    //멤버십 확인
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
           "FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId")
    boolean isUserMemberOfCrew(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //크루장 확인
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
           "FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId " +
           "AND cm.role = 'OWNER'")
    boolean isUserOwnerOfCrew(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //멤버 제거 (물리 삭제)
    @Modifying
    @Query("DELETE FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId")
    int deleteByCrewIdAndUserId(@Param("crewId") Long crewId, @Param("userId") Long userId);

    //일반 멤버만 조회
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "WHERE cm.crew = :crew " +
           "AND cm.role = 'MEMBER' " +
           "ORDER BY cm.joinedAt ASC")
    List<CrewMemberEntity> findActiveMembers(@Param("crew") CrewEntity crew);

    //멤버십 조회
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.user " +
           "WHERE cm.user.id = :userId AND cm.crew.id = :crewId")
    Optional<CrewMemberEntity> findMembership(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //DB 페이징을 사용한 크루 멤버 조회 (성능 최적화)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.user " +
           "WHERE cm.crew.id = :crewId")
    Page<CrewMemberEntity> findCrewMembersWithPaging(@Param("crewId") Long crewId, Pageable pageable);

    // 사용자 ID로 크루 멤버십 일괄 삭제 (회원 탈퇴용)
    void deleteByUserId(Long userId);

    // 크루 ID로 실시간 멤버 수 카운트 (Race Condition 방지)
    @Query("SELECT COUNT(cm) FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId")
    long countByCrewId(@Param("crewId") Long crewId);
}