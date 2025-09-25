package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrewService {

    /**
     * 크루 생성
     */
    CrewEntity createCrew(AuthenticatedUser user, String name, String description,
                         Integer maxMembers, String profileImageUrl);

    /**
     * 크루 조회 (단건)
     */
    CrewEntity getCrewById(Long crewId);

    /**
     * 크루 목록 조회 (페이징)
     */
    Page<CrewEntity> getCrews(Pageable pageable);

    /**
     * 크루 검색 (이름으로)
     */
    Page<CrewEntity> searchCrewsByName(String name, Pageable pageable);

    /**
     * 크루 정보 수정 (크루장만 가능)
     */
    CrewEntity updateCrew(AuthenticatedUser user, Long crewId, String name,
                         String description, Integer maxMembers, String profileImageUrl);

    /**
     * 크루 삭제 (크루장만 가능)
     */
    void deleteCrew(AuthenticatedUser user, Long crewId);

    /**
     * 사용자의 크루 목록 조회
     */
    Page<CrewEntity> getUserCrews(AuthenticatedUser user, Pageable pageable);

    /**
     * 크루 활성화/비활성화 (크루장만 가능)
     */
    CrewEntity toggleCrewStatus(AuthenticatedUser user, Long crewId);

    /**
     * 크루 소유자인지 확인
     */
    boolean isCrewOwner(Long crewId, Long userId);

    /**
     * 크루 멤버인지 확인
     */
    boolean isCrewMember(Long crewId, Long userId);

    /**
     * 지역별 크루 조회
     */
    Page<CrewEntity> findCrewsByRegion(String region, Pageable pageable);

    /**
     * 활성화된 모든 크루 조회
     */
    Page<CrewEntity> findAllActiveCrews(Pageable pageable);

    /**
     * 크루 생성 (간단 버전)
     */
    CrewEntity createCrew(Long userId, CrewEntity crewData);

    /**
     * 크루 정보 수정 (간단 버전)
     */
    CrewEntity updateCrew(Long userId, Long crewId, CrewEntity updateData);

    /**
     * 크루 삭제 (간단 버전)
     */
    void deleteCrew(Long userId, Long crewId);

    /**
     * 크루 멤버십 검증 (예외 발생)
     */
    void validateCrewMembership(Long userId, Long crewId);

    /**
     * 크루 프로필 이미지 삭제 (크루장만 가능)
     */
    void removeCrewProfileImage(AuthenticatedUser user, Long crewId);
}