package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.enums.CrewRole;
import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMemberEntity, Long> {

    List<CrewMemberEntity> findByCrewAndIsActiveTrueOrderByJoinedAtAsc(CrewEntity crew);

    Optional<CrewMemberEntity> findByCrewAndUserAndIsActiveTrue(CrewEntity crew, User user);

    List<CrewMemberEntity> findByCrewAndRoleAndIsActiveTrue(CrewEntity crew, CrewRole role);

    Optional<CrewMemberEntity> findByCrewAndRole(CrewEntity crew, CrewRole role);

    List<CrewMemberEntity> findByUserAndIsActiveTrueOrderByJoinedAtDesc(User user);

    boolean existsByCrewAndUserAndIsActiveTrue(CrewEntity crew, User user);

    long countByCrewAndIsActiveTrue(CrewEntity crew);

    //N+1 해결을 위한 fetch join
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.user " +
           "WHERE cm.crew = :crew AND cm.isActive = true " +
           "ORDER BY cm.joinedAt ASC")
    List<CrewMemberEntity> findByCrewWithUser(@Param("crew") CrewEntity crew);

    //사용자별 크루 조회 (크루 정보 포함)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.crew " +
           "WHERE cm.user = :user AND cm.isActive = true " +
           "ORDER BY cm.joinedAt DESC")
    List<CrewMemberEntity> findByUserWithCrew(@Param("user") User user);

    //멤버십 확인
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
           "FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId AND cm.isActive = true")
    boolean isUserMemberOfCrew(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //크루장 확인
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
           "FROM CrewMemberEntity cm " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId " +
           "AND cm.role = 'OWNER' AND cm.isActive = true")
    boolean isUserOwnerOfCrew(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //멤버 제거 (소프트 삭제)
    @Modifying
    @Query("UPDATE CrewMemberEntity cm SET cm.isActive = false " +
           "WHERE cm.crew.id = :crewId AND cm.user.id = :userId")
    int removeUserFromCrew(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //일반 멤버만 조회
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "WHERE cm.crew = :crew AND cm.isActive = true " +
           "AND cm.role = 'MEMBER' " +
           "ORDER BY cm.joinedAt ASC")
    List<CrewMemberEntity> findActiveMembers(@Param("crew") CrewEntity crew);

    //랭킹용 멤버 조회 (사용자 정보 포함)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "JOIN FETCH cm.user " +
           "WHERE cm.crew.id = :crewId AND cm.isActive = true " +
           "ORDER BY cm.joinedAt ASC")
    List<CrewMemberEntity> findMembersForRanking(@Param("crewId") Long crewId);

    //멤버십 조회 (활성/비활성 무관)
    @Query("SELECT cm FROM CrewMemberEntity cm " +
           "WHERE cm.user.id = :userId AND cm.crew.id = :crewId")
    Optional<CrewMemberEntity> findMembership(@Param("userId") Long userId, @Param("crewId") Long crewId);

    //크루의 모든 멤버 비활성화 (크루 삭제 시 사용)
    @Modifying
    @Query("UPDATE CrewMemberEntity cm SET cm.isActive = false " +
           "WHERE cm.crew.id = :crewId")
    int deactivateAllMembersInCrew(@Param("crewId") Long crewId);
}