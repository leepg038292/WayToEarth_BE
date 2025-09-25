package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CrewServiceImpl implements CrewService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final CrewStatisticsService crewStatisticsService;

    @Override
    @Transactional
    public CrewEntity createCrew(AuthenticatedUser user, String name, String description,
                                Integer maxMembers, String profileImageUrl) {
        // 사용자 조회
        User owner = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 크루 생성
        CrewEntity crew = CrewEntity.builder()
                .name(name)
                .description(description)
                .maxMembers(maxMembers != null ? maxMembers : 50)
                .profileImageUrl(profileImageUrl)
                .owner(owner)
                .isActive(true)
                .build();

        CrewEntity savedCrew = crewRepository.save(crew);

        // 크루 소유자를 멤버로 추가
        CrewMemberEntity ownerMember = CrewMemberEntity.createOwner(savedCrew, owner);
        crewMemberRepository.save(ownerMember);

        // 현재 멤버 수 업데이트
        savedCrew.incrementMemberCount();

        log.info("크루가 생성되었습니다. crewId: {}, ownerId: {}", savedCrew.getId(), user.getUserId());
        return savedCrew;
    }

    @Override
    public CrewEntity getCrewById(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    @Override
    public Page<CrewEntity> getCrews(Pageable pageable) {
        // 기존 List 메서드를 사용하되 Page로 변환 필요
        // 일단 간단하게 findAll 사용 - 추후 Repository에 Pageable 메서드 추가
        return crewRepository.findAll(pageable);
    }

    @Override
    public Page<CrewEntity> searchCrewsByName(String name, Pageable pageable) {
        // 마찬가지로 추후 Repository에 Pageable 메서드 추가 필요
        return crewRepository.findAll(pageable); // 임시
    }

    @Override
    @Transactional
    public CrewEntity updateCrew(AuthenticatedUser user, Long crewId, String name,
                                String description, Integer maxMembers, String profileImageUrl) {
        CrewEntity crew = getCrewById(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crewId, user.getUserId())) {
            throw new RuntimeException("크루 정보 수정은 크루장만 가능합니다.");
        }

        // 현재 멤버 수보다 적게 설정할 수 없음
        int currentMemberCount = crew.getCurrentMemberCount();
        if (maxMembers != null && maxMembers < currentMemberCount) {
            throw new RuntimeException("현재 멤버 수(" + currentMemberCount + ")보다 적게 설정할 수 없습니다.");
        }

        // 정보 업데이트
        if (name != null) crew.setName(name);
        if (description != null) crew.setDescription(description);
        if (maxMembers != null) crew.setMaxMembers(maxMembers);
        if (profileImageUrl != null) crew.setProfileImageUrl(profileImageUrl);

        log.info("크루 정보가 수정되었습니다. crewId: {}, userId: {}", crewId, user.getUserId());
        return crew;
    }

    @Override
    @Transactional
    public void deleteCrew(AuthenticatedUser user, Long crewId) {
        CrewEntity crew = getCrewById(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crewId, user.getUserId())) {
            throw new RuntimeException("크루 삭제는 크루장만 가능합니다.");
        }

        // 소프트 삭제 (비활성화)
        crew.setIsActive(false);

        log.info("크루가 삭제되었습니다. crewId: {}, userId: {}", crewId, user.getUserId());
    }

    @Override
    public Page<CrewEntity> getUserCrews(AuthenticatedUser user, Pageable pageable) {
        User userEntity = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // Repository에 Pageable 지원 메서드 추가 필요 - 임시로 간단히 구현
        return crewRepository.findAll(pageable); // 임시
    }

    @Override
    @Transactional
    public CrewEntity toggleCrewStatus(AuthenticatedUser user, Long crewId) {
        CrewEntity crew = getCrewById(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crewId, user.getUserId())) {
            throw new RuntimeException("크루 상태 변경은 크루장만 가능합니다.");
        }

        crew.setIsActive(!crew.getIsActive());

        log.info("크루 상태가 변경되었습니다. crewId: {}, isActive: {}, userId: {}",
                crewId, crew.getIsActive(), user.getUserId());
        return crew;
    }

    @Override
    public boolean isCrewOwner(Long crewId, Long userId) {
        return crewMemberRepository.isUserOwnerOfCrew(userId, crewId);
    }

    @Override
    public boolean isCrewMember(Long crewId, Long userId) {
        return crewMemberRepository.isUserMemberOfCrew(userId, crewId);
    }

    @Override
    public Page<CrewEntity> findCrewsByRegion(String region, Pageable pageable) {
        return crewRepository.findByRegionAndIsActiveTrueOrderByCreatedAtDesc(region, pageable);
    }

    @Override
    public Page<CrewEntity> findAllActiveCrews(Pageable pageable) {
        return crewRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }

    @Override
    public CrewEntity createCrew(Long userId, CrewEntity crewData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));

        CrewEntity crew = CrewEntity.builder()
                .name(crewData.getName())
                .description(crewData.getDescription())
                .maxMembers(crewData.getMaxMembers())
                .profileImageUrl(crewData.getProfileImageUrl())
                .owner(user)
                .isActive(true)
                .currentMembers(1) // 생성자가 첫 멤버
                .build();

        CrewEntity savedCrew = crewRepository.save(crew);

        // 크루 생성자를 OWNER로 추가
        CrewMemberEntity owner = CrewMemberEntity.createOwner(savedCrew, user);
        crewMemberRepository.save(owner);

        // 현재 멤버 수 업데이트 (실제 멤버 수로 동기화)
        savedCrew.incrementMemberCount();

        log.info("새 크루가 생성되었습니다. crewId: {}, name: {}, ownerId: {}",
                savedCrew.getId(), savedCrew.getName(), userId);

        return savedCrew;
    }

    @Override
    public CrewEntity updateCrew(Long userId, Long crewId, CrewEntity updateData) {
        CrewEntity crew = getCrewById(crewId);

        if (!isCrewOwner(crewId, userId)) {
            throw new RuntimeException("크루 정보 수정은 크루장만 가능합니다.");
        }

        if (updateData.getName() != null) crew.setName(updateData.getName());
        if (updateData.getDescription() != null) crew.setDescription(updateData.getDescription());
        if (updateData.getMaxMembers() != null) crew.setMaxMembers(updateData.getMaxMembers());
        if (updateData.getProfileImageUrl() != null) crew.setProfileImageUrl(updateData.getProfileImageUrl());

        log.info("크루 정보가 수정되었습니다. crewId: {}, userId: {}", crewId, userId);
        return crew;
    }

    @Override
    public void deleteCrew(Long userId, Long crewId) {
        CrewEntity crew = getCrewById(crewId);

        if (!isCrewOwner(crewId, userId)) {
            throw new RuntimeException("크루 삭제는 크루장만 가능합니다.");
        }

        crew.setIsActive(false);

        // 관련 통계 정리
        crewStatisticsService.cleanupStatisticsForCrew(crewId);

        log.info("크루가 삭제되었습니다. crewId: {}, userId: {}", crewId, userId);
    }

    @Override
    public void validateCrewMembership(Long userId, Long crewId) {
        if (!isCrewMember(crewId, userId)) {
            throw new RuntimeException("크루 멤버만 접근할 수 있습니다. crewId: " + crewId);
        }
    }
}