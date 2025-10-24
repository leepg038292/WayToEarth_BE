package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity.JoinRequestStatus;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrewJoinService {

    /**
     * 크루 가입 신청
     */
    CrewJoinRequestEntity requestToJoinCrew(AuthenticatedUser user, Long crewId, String message);

    /**
     * 가입 신청 승인 (크루장만 가능)
     */
    void approveJoinRequest(AuthenticatedUser user, Long requestId);

    /**
     * 가입 신청 거부 (크루장만 가능)
     */
    void rejectJoinRequest(AuthenticatedUser user, Long requestId, String reason);

    /**
     * 가입 신청 취소 (신청자 본인만 가능)
     */
    void cancelJoinRequest(AuthenticatedUser user, Long requestId);

    /**
     * 크루별 가입 신청 목록 조회 (크루장만)
     */
    Page<CrewJoinRequestEntity> getCrewJoinRequests(AuthenticatedUser user, Long crewId,
                                                   JoinRequestStatus status, Pageable pageable);

    /**
     * 사용자별 가입 신청 내역 조회
     */
    List<CrewJoinRequestEntity> getUserJoinRequests(AuthenticatedUser user);

    /**
     * 가입 신청 상세 조회
     */
    CrewJoinRequestEntity getJoinRequest(Long requestId);

    /**
     * 특정 크루에 대한 사용자의 가입 신청 상태 확인
     */
    CrewJoinRequestEntity getUserJoinRequestForCrew(AuthenticatedUser user, Long crewId);

    /**
     * 가입 가능한 크루들 조회 (사용자 기준)
     */
    List<Long> getJoinableCrewIds(AuthenticatedUser user);

    /**
     * 크루 가입 가능 여부 확인
     */
    boolean canJoinCrew(AuthenticatedUser user, Long crewId);

    /**
     * 대기 중인 가입 신청 수 조회 (크루장용)
     */
    long getPendingRequestCount(Long crewId);
}