package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.CrewNotFoundException;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UnauthorizedAccessException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.crew.CrewJoinRequestRepository;
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
public class CrewServiceImpl implements CrewService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewJoinRequestRepository crewJoinRequestRepository;
    private final UserRepository userRepository;
    private final CrewStatisticsService crewStatisticsService;
    private final FileService fileService;
    private final com.waytoearth.repository.crew.CrewChatNotificationSettingRepository crewChatNotificationSettingRepository;
    private final com.waytoearth.repository.crew.CrewChatRepository crewChatRepository;
    private final com.waytoearth.service.ranking.CrewRankingService crewRankingService;

    @Override
    @Transactional
    public CrewEntity createCrew(AuthenticatedUser user, String name, String description,
                                Integer maxMembers, String profileImageUrl) {
        // 사용자 조회
        User owner = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException(user.getUserId()));

        // 크루장 중복 생성 방지
        List<CrewEntity> ownedCrews = crewRepository.findByOwner(owner);
        if (!ownedCrews.isEmpty()) {
            String existingCrewName = ownedCrews.get(0).getName();
            String message = String.format(
                "이미 크루를 소유하고 있습니다. 한 사용자는 하나의 크루만 생성할 수 있습니다. (소유 중인 크루: %s)",
                existingCrewName
            );
            log.warn("[CrewService] 크루 중복 생성 시도 차단 - userId: {}, existingCrew: {}",
                     user.getUserId(), existingCrewName);
            throw new com.waytoearth.exception.CrewAlreadyOwnedException(message);
        }

        // 이름 유효성 추가 검증 (서버 측 보강)
        validateCrewName(name);

        // 크루 생성
        CrewEntity crew = CrewEntity.builder()
                .name(name)
                .description(description)
                .maxMembers(maxMembers != null ? maxMembers : 50)
                .profileImageUrl(profileImageUrl)
                .owner(owner)
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
        return crewRepository.findByIdWithOwner(crewId)
                .orElseThrow(() -> new CrewNotFoundException("크루를 찾을 수 없습니다. crewId: " + crewId));
    }

    @Override
    public Page<CrewEntity> getCrews(Pageable pageable) {
        // 기존 List 메서드를 사용하되 Page로 변환 필요
        // 일단 간단하게 findAll 사용 - 추후 Repository에 Pageable 메서드 추가
        return crewRepository.findAll(pageable);
    }

    @Override
    public Page<CrewEntity> searchCrewsByName(String name, Pageable pageable) {
        // N+1 방지 및 DB 네이티브 페이징 사용
        return crewRepository.findByNameContainingWithOwner(name, pageable);
    }

    @Override
    @Transactional
    public CrewEntity updateCrew(AuthenticatedUser user, Long crewId, String name,
                                String description, Integer maxMembers, String profileImageUrl, String profileImageKey) {
        CrewEntity crew = getCrewById(crewId);

        // 크루장인지 확인
        if (!isCrewOwner(crewId, user.getUserId())) {
            throw new UnauthorizedAccessException("크루 정보 수정은 크루장만 가능합니다.");
        }

        // 현재 멤버 수보다 적게 설정할 수 없음
        int currentMemberCount = crew.getCurrentMemberCount();
        if (maxMembers != null && maxMembers < currentMemberCount) {
            throw new InvalidParameterException("현재 멤버 수(" + currentMemberCount + ")보다 적게 설정할 수 없습니다.");
        }

        // 정보 업데이트
        if (name != null) {
            validateCrewName(name);
            crew.setName(name);
        }
        if (description != null) crew.setDescription(description);
        if (maxMembers != null) crew.setMaxMembers(maxMembers);
        if (profileImageUrl != null) crew.setProfileImageUrl(profileImageUrl);
        if (profileImageKey != null) crew.setProfileImageKey(profileImageKey);

        log.info("크루 정보가 수정되었습니다. crewId: {}, userId: {}", crewId, user.getUserId());
        return crew;
    }

    @Override
    public Page<CrewEntity> getUserCrews(AuthenticatedUser user, Pageable pageable) {
        User userEntity = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException(user.getUserId()));
        // N+1 방지 및 DB 네이티브 페이징 사용
        return crewRepository.findCrewsByUserWithOwnerPaged(userEntity, pageable);
    }

    private void validateCrewName(String name) {
        if (name == null) return;
        String pattern = "^[가-힣a-zA-Z0-9 _-]+$";
        if (!name.matches(pattern)) {
            throw new InvalidParameterException("크루 이름은 한글, 영문, 숫자, 공백, '-', '_'만 가능합니다.");
        }
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
        // region 필드가 CrewEntity에 없으므로 전체 크루 반환
        return crewRepository.findAllWithOwner(pageable);
    }

    @Override
    public Page<CrewEntity> findAllActiveCrews(Pageable pageable) {
        return crewRepository.findAllWithOwner(pageable);
    }

    @Override
    @Transactional
    public void deleteCrew(Long userId, Long crewId) {
        CrewEntity crew = getCrewById(crewId);

        if (!isCrewOwner(crewId, userId)) {
            throw new RuntimeException("크루 삭제는 크루장만 가능합니다.");
        }

        // 1. S3에서 프로필 이미지 삭제
        if (crew.getProfileImageUrl() != null && !crew.getProfileImageUrl().isEmpty()) {
            String imageKey = extractS3KeyFromUrl(crew.getProfileImageUrl());
            if (imageKey != null) {
                fileService.deleteObject(imageKey);
            }
        }

        // 2. CASCADE로 자동 삭제되지 않는 데이터 수동 삭제
        crewStatisticsService.cleanupStatisticsForCrew(crewId);
        crewChatNotificationSettingRepository.deleteAllByCrew_Id(crewId);

        // 3. Redis 랭킹 데이터 삭제
        crewRankingService.removeCrewFromAllRankings(crewId);

        // 4. 크루 물리 삭제 (CASCADE로 멤버, 가입신청 자동 삭제)
        // 채팅 메시지는 ON DELETE SET NULL로 자동으로 crew_id가 NULL이 됨 (메시지 보존)
        crewRepository.deleteById(crewId);

        log.info("크루와 연관 데이터가 물리 삭제되었습니다 (채팅 메시지는 보존됨). crewId: {}, userId: {}", crewId, userId);
    }

    @Override
    public void validateCrewMembership(Long userId, Long crewId) {
        if (!isCrewMember(crewId, userId)) {
            throw new RuntimeException("크루 멤버만 접근할 수 있습니다. crewId: " + crewId);
        }
    }

    @Override
    @Transactional
    public void removeCrewProfileImage(AuthenticatedUser user, Long crewId) {
        // 크루 존재 확인
        CrewEntity crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new CrewNotFoundException(crewId));

        // 크루장 권한 확인
        CrewMemberEntity membership = crewMemberRepository.findMembership(user.getUserId(), crewId)
                .orElseThrow(() -> new UnauthorizedAccessException("크루 멤버가 아닙니다."));

        if (!membership.isOwner()) {
            throw new UnauthorizedAccessException("크루장만 프로필 이미지를 삭제할 수 있습니다.");
        }

        // 기존 이미지가 있다면 S3에서 삭제
        // profileImageKey 우선 사용, 없으면 URL에서 추출
        String imageKey = crew.getProfileImageKey();
        if (imageKey == null || imageKey.isEmpty()) {
            if (crew.getProfileImageUrl() != null && !crew.getProfileImageUrl().isEmpty()) {
                // 기존 데이터 호환성: URL에서 S3 키 추출 (예: crews/123/profile.jpg)
                imageKey = extractS3KeyFromUrl(crew.getProfileImageUrl());
            }
        }

        if (imageKey != null && !imageKey.isEmpty()) {
            fileService.deleteObject(imageKey);
        }

        // 데이터베이스에서 프로필 이미지 URL 및 Key 제거
        crew.setProfileImageUrl(null);
        crew.setProfileImageKey(null);
        crewRepository.save(crew);

        log.info("[Crew Profile Image Delete] crewId={}, userId={}, key={}", crewId, user.getUserId(), imageKey);
    }

    /**
     * S3 URL에서 오브젝트 키를 추출하는 유틸리티 메서드
     * 예: https://bucket.s3.amazonaws.com/crews/123/profile.jpg → crews/123/profile.jpg
     */
    private String extractS3KeyFromUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }

        try {
            // crews/로 시작하는 키 패턴 찾기
            if (s3Url.contains("crews/")) {
                int index = s3Url.indexOf("crews/");
                String keyPart = s3Url.substring(index);
                // 쿼리 파라미터 제거
                int queryIndex = keyPart.indexOf('?');
                if (queryIndex != -1) {
                    keyPart = keyPart.substring(0, queryIndex);
                }
                return keyPart;
            }
        } catch (Exception e) {
            log.warn("S3 URL에서 키 추출 실패: {}", s3Url, e);
        }

        return null;
    }
}
