package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.enums.CrewRole;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrewMemberService {

    /**
     * 크루 멤버 목록 조회 (페이징)
     */
    Page<CrewMemberEntity> getCrewMembers(Long crewId, Pageable pageable);

    /**
     * 크루 멤버 목록 조회 (전체 - 사용자 정보 포함)
     */
    List<CrewMemberEntity> getCrewMembersWithUser(Long crewId);

    /**
     * 크루 멤버 추방 (크루장만 가능)
     */
    void removeMemberFromCrew(AuthenticatedUser user, Long crewId, Long targetUserId);

    /**
     * 크루 탈퇴 (본인)
     */
    void leaveCrew(AuthenticatedUser user, Long crewId);

    /**
     * 크루 멤버 역할 변경 (크루장만 가능)
     */
    CrewMemberEntity changeMemberRole(AuthenticatedUser user, Long crewId, Long targetUserId, CrewRole newRole);

    /**
     * 사용자별 가입한 크루 목록
     */
    List<CrewMemberEntity> getUserCrewMemberships(AuthenticatedUser user);

    /**
     * 특정 크루의 멤버십 정보 조회
     */
    CrewMemberEntity getCrewMembership(Long crewId, Long userId);

    /**
     * 크루 멤버 수 조회
     */
    long getCrewMemberCount(Long crewId);

    /**
     * 크루 활성 멤버 수 조회 (isActive = true)
     */
    long getActiveCrewMemberCount(Long crewId);

    /**
     * 크루장 변경 (현재 크루장만 가능)
     */
    void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId);

    /**
     * 크루의 일반 멤버들만 조회
     */
    List<CrewMemberEntity> getRegularMembers(Long crewId);
}