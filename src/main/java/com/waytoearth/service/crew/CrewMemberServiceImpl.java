package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.enums.CrewRole;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CrewMemberServiceImpl implements CrewMemberService {

    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CrewMemberEntity> getCrewMembers(Long crewId, Pageable pageable) {
        // 크루 존재 확인
        getCrewEntity(crewId);

        // DB 네이티브 페이징 사용으로 성능 최적화
        return crewMemberRepository.findCrewMembersWithPaging(crewId, pageable);
    }

    @Override
    public List<CrewMemberEntity> getCrewMembersWithUser(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        return crewMemberRepository.findByCrewWithUser(crew);
    }

    @Override
    @Transactional
    public void removeMemberFromCrew(AuthenticatedUser user, Long crewId, Long targetUserId) {
        CrewEntity crew = getCrewEntity(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("멤버 추방은 크루장만 가능합니다.");
        }

        // 자기 자신은 추방할 수 없음
        if (user.getUserId().equals(targetUserId)) {
            throw new RuntimeException("크루장은 자신을 추방할 수 없습니다. 크루장 권한을 이양하세요.");
        }

        // 대상 멤버 조회 및 존재 확인
        crewMemberRepository.findMembership(targetUserId, crewId)
                .orElseThrow(() -> new RuntimeException("해당 사용자는 크루 멤버가 아닙니다."));

        // 물리 삭제
        int affected = crewMemberRepository.deleteByCrewIdAndUserId(crewId, targetUserId);
        if (affected == 0) {
            throw new RuntimeException("멤버 추방에 실패했습니다.");
        }

        // 크루 멤버 수 감소
        crew.decrementMemberCount();

        log.info("크루 멤버가 추방되었습니다. crewId: {}, targetUserId: {}, removedBy: {}",
                crewId, targetUserId, user.getUserId());
    }

    @Override
    @Transactional
    public void leaveCrew(AuthenticatedUser user, Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);

        // 크루장인 경우 탈퇴 불가
        if (isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("크루장은 탈퇴할 수 없습니다. 크루장 권한을 이양하거나 크루를 삭제하세요.");
        }

        // 멤버십 확인
        crewMemberRepository.findMembership(user.getUserId(), crewId)
                .orElseThrow(() -> new RuntimeException("해당 크루의 멤버가 아닙니다."));

        // 물리 삭제
        int affected = crewMemberRepository.deleteByCrewIdAndUserId(crewId, user.getUserId());
        if (affected == 0) {
            throw new RuntimeException("크루 탈퇴에 실패했습니다.");
        }

        // 크루 멤버 수 감소
        crew.decrementMemberCount();

        log.info("사용자가 크루에서 탈퇴했습니다. crewId: {}, userId: {}", crewId, user.getUserId());
    }

    @Override
    @Transactional
    public CrewMemberEntity changeMemberRole(AuthenticatedUser user, Long crewId, Long targetUserId, CrewRole newRole) {
        CrewEntity crew = getCrewEntity(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("멤버 역할 변경은 크루장만 가능합니다.");
        }

        // 자기 자신의 역할은 변경할 수 없음
        if (user.getUserId().equals(targetUserId)) {
            throw new RuntimeException("자신의 역할은 변경할 수 없습니다.");
        }

        // OWNER 역할로는 변경 불가 (transferOwnership 사용)
        if (newRole == CrewRole.OWNER) {
            throw new RuntimeException("크루장 권한 이양은 별도 기능을 사용하세요.");
        }

        // 대상 멤버 조회
        CrewMemberEntity targetMember = crewMemberRepository.findMembership(targetUserId, crewId)
                .orElseThrow(() -> new RuntimeException("해당 사용자는 크루 멤버가 아닙니다."));

        targetMember.setRole(newRole);

        log.info("크루 멤버 역할이 변경되었습니다. crewId: {}, targetUserId: {}, newRole: {}, changedBy: {}",
                crewId, targetUserId, newRole, user.getUserId());

        return targetMember;
    }

    @Override
    public List<CrewMemberEntity> getUserCrewMemberships(AuthenticatedUser user) {
        User userEntity = getUserEntity(user.getUserId());
        return crewMemberRepository.findByUserWithCrew(userEntity);
    }

    @Override
    public CrewMemberEntity getCrewMembership(Long crewId, Long userId) {
        return crewMemberRepository.findMembership(userId, crewId)
                .orElseThrow(() -> new RuntimeException("해당 사용자의 크루 멤버십을 찾을 수 없습니다."));
    }

    @Override
    public long getCrewMemberCount(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        return crewMemberRepository.countByCrewAndIsActiveTrue(crew);
    }

    @Override
    public long getActiveCrewMemberCount(Long crewId) {
        return getCrewMemberCount(crewId); // 동일한 로직
    }

    @Override
    @Transactional
    public void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId) {
        CrewEntity crew = getCrewEntity(crewId);

        // 현재 크루장인지 확인
        if (!isCrewOwner(crew, user.getUserId())) {
            throw new RuntimeException("크루장 권한 이양은 현재 크루장만 가능합니다.");
        }

        // 자기 자신에게는 이양 불가
        if (user.getUserId().equals(newOwnerId)) {
            throw new RuntimeException("자기 자신에게는 권한을 이양할 수 없습니다.");
        }

        // 새 크루장이 멤버인지 확인
        CrewMemberEntity newOwnerMember = crewMemberRepository.findMembership(newOwnerId, crewId)
                .orElseThrow(() -> new RuntimeException("새 크루장은 해당 크루의 멤버여야 합니다."));

        // 현재 크루장을 일반 멤버로 변경
        CrewMemberEntity currentOwnerMember = crewMemberRepository.findMembership(user.getUserId(), crewId)
                .orElseThrow(() -> new RuntimeException("현재 크루장의 멤버십을 찾을 수 없습니다."));
        currentOwnerMember.setRole(CrewRole.MEMBER);

        // 새 크루장으로 변경
        newOwnerMember.setRole(CrewRole.OWNER);

        // 크루 엔티티의 소유자도 변경
        User newOwnerUser = getUserEntity(newOwnerId);
        crew.setOwner(newOwnerUser);

        log.info("크루장 권한이 이양되었습니다. crewId: {}, fromUserId: {}, toUserId: {}",
                crewId, user.getUserId(), newOwnerId);
    }

    @Override
    public List<CrewMemberEntity> getRegularMembers(Long crewId) {
        CrewEntity crew = getCrewEntity(crewId);
        return crewMemberRepository.findActiveMembers(crew);
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

    @Override
    public boolean isCrewMember(Long crewId, Long userId) {
        return crewMemberRepository.isUserMemberOfCrew(userId, crewId);
    }
}