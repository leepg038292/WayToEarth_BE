package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity.JoinRequestStatus;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewJoinRequestRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CrewJoinServiceImpl implements CrewJoinService {

    private final CrewJoinRequestRepository joinRequestRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public CrewJoinRequestEntity requestToJoinCrew(AuthenticatedUser user, Long crewId, String message) {
        CrewEntity crew = getCrewEntity(crewId);
        User userEntity = getUserEntity(user.getUserId());

        // 가입 가능 여부 확인
        if (!canJoinCrew(user, crewId)) {
            throw new RuntimeException("해당 크루에 가입할 수 없습니다.");
        }

        // 크루 인원이 가득 찬지 확인
        if (crew.isFull()) {
            throw new RuntimeException("크루 정원이 가득 찼습니다.");
        }

        // 가입 신청 생성
        CrewJoinRequestEntity joinRequest = CrewJoinRequestEntity.builder()
                .crew(crew)
                .user(userEntity)
                .message(message)
                .status(JoinRequestStatus.PENDING)
                .build();

        CrewJoinRequestEntity savedRequest = joinRequestRepository.save(joinRequest);

        log.info("크루 가입 신청이 생성되었습니다. requestId: {}, crewId: {}, userId: {}",
                savedRequest.getId(), crewId, user.getUserId());

        return savedRequest;
    }

    @Override
    @Transactional
    public void approveJoinRequest(AuthenticatedUser user, Long requestId) {
        CrewJoinRequestEntity joinRequest = joinRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new RuntimeException("가입 요청을 찾을 수 없습니다. requestId: " + requestId));
        CrewEntity crew = crewRepository.findByIdForUpdate(joinRequest.getCrew().getId())
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + joinRequest.getCrew().getId()));

        // 크루장인지 확인
        if (!isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("가입 신청 승인은 크루장만 가능합니다.");
        }

        // 신청 상태 확인
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new RuntimeException("이미 처리된 가입 신청입니다.");
        }

        // 실시간 멤버 수 확인 (Race Condition 방지)
        long actualMemberCount = crewMemberRepository.countByCrewId(crew.getId());
        if (actualMemberCount >= crew.getMaxMembers()) {
            throw new RuntimeException("크루 정원이 초과되었습니다. (현재: " + actualMemberCount + "명)");
        }

        // 가입 신청 승인
        joinRequest.approve(getUserEntity(user.getUserId()), "가입 승인");
        joinRequestRepository.saveAndFlush(joinRequest);  // 즉시 DB 반영

        // 새로운 멤버 추가 (물리 삭제로 인해 탈퇴한 멤버는 DB에 없음)
        CrewMemberEntity newMember = CrewMemberEntity.createMember(crew, joinRequest.getUser());
        crewMemberRepository.save(newMember);
        log.info("새 멤버 추가 - requestId: {}, userId: {}", requestId, newMember.getUser().getId());

        // 크루 멤버 수 증가 (낙관적 락으로 동시성 제어)
        crew.incrementMemberCount();

        log.info("크루 가입 신청이 승인되었습니다. requestId: {}, approvedBy: {}, newMemberId: {}, actualCount: {}",
                requestId, user.getUserId(), joinRequest.getUser().getId(), actualMemberCount + 1);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(AuthenticatedUser user, Long requestId, String reason) {
        CrewJoinRequestEntity joinRequest = getJoinRequest(requestId);

        // 크루장인지 확인
        if (!isCrewOwner(joinRequest.getCrew(), user.getUserId())) {
            throw new RuntimeException("가입 신청 거부는 크루장만 가능합니다.");
        }

        // 신청 상태 확인
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new RuntimeException("이미 처리된 가입 신청입니다.");
        }

        // 가입 신청 거부
        joinRequest.reject(getUserEntity(user.getUserId()), reason);

        log.info("크루 가입 신청이 거부되었습니다. requestId: {}, rejectedBy: {}, reason: {}",
                requestId, user.getUserId(), reason);
    }

    @Override
    @Transactional
    public void cancelJoinRequest(AuthenticatedUser user, Long requestId) {
        CrewJoinRequestEntity joinRequest = getJoinRequest(requestId);

        // 신청자 본인인지 확인
        if (!joinRequest.getUser().getId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 신청한 가입 신청만 취소할 수 있습니다.");
        }

        // 신청 상태 확인
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new RuntimeException("이미 처리된 가입 신청은 취소할 수 없습니다.");
        }

        // 가입 신청 취소
        joinRequest.cancel();

        log.info("크루 가입 신청이 취소되었습니다. requestId: {}, cancelledBy: {}",
                requestId, user.getUserId());
    }

    @Override
    public Page<CrewJoinRequestEntity> getCrewJoinRequests(AuthenticatedUser user, Long crewId,
                                                          JoinRequestStatus status, Pageable pageable) {
        CrewEntity crew = getCrewEntity(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("가입 신청 목록 조회는 크루장만 가능합니다.");
        }

        // DB 네이티브 페이징 사용으로 성능 최적화 (status null 허용)
        return joinRequestRepository.findCrewJoinRequestsWithPaging(crewId, status, pageable);
    }

    @Override
    public List<CrewJoinRequestEntity> getUserJoinRequests(AuthenticatedUser user) {
        User userEntity = getUserEntity(user.getUserId());
        return joinRequestRepository.findByUserOrderByCreatedAtDesc(userEntity);
    }

    @Override
    public CrewJoinRequestEntity getJoinRequest(Long requestId) {
        return joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("가입 신청을 찾을 수 없습니다. requestId: " + requestId));
    }

    @Override
    public CrewJoinRequestEntity getUserJoinRequestForCrew(AuthenticatedUser user, Long crewId) {
        return joinRequestRepository.findUserRequest(user.getUserId(), crewId)
                .orElse(null); // 신청 내역이 없으면 null 반환
    }

    @Override
    public List<Long> getJoinableCrewIds(AuthenticatedUser user) {
        User userEntity = getUserEntity(user.getUserId());
        return crewRepository.findJoinableCrews(userEntity).stream()
                .map(CrewEntity::getId)
                .toList();
    }

    @Override
    public boolean canJoinCrew(AuthenticatedUser user, Long crewId) {
        // 1. 이미 멤버인지 확인
        if (crewMemberRepository.isUserMemberOfCrew(user.getUserId(), crewId)) {
            return false;
        }

        // 2. 대기 중인 가입 신청이 있는지 확인
        CrewJoinRequestEntity existingRequest = getUserJoinRequestForCrew(user, crewId);
        if (existingRequest != null && existingRequest.getStatus() == JoinRequestStatus.PENDING) {
            return false;
        }

        // 3. 크루 정원이 가득 찼는지 확인
        CrewEntity crew = getCrewEntity(crewId);
        if (crew.isFull()) {
            return false;
        }

        return true;
    }

    @Override
    public long getPendingRequestCount(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        return joinRequestRepository.countByCrewAndStatus(crew, JoinRequestStatus.PENDING);
    }

    // Private helper methods
    private CrewEntity getCrewEntity(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
    }

    private boolean isCrewOwner(CrewEntity crew, Long userId) {
        return crew.getOwner().getId().equals(userId);
    }
}
