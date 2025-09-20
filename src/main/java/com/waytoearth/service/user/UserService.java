package com.waytoearth.service.user;

import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.request.user.UserUpdateRequest;
import com.waytoearth.dto.response.user.UserInfoResponse;
import com.waytoearth.dto.response.user.UserSummaryResponse;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.DuplicateResourceException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.emblem.EmblemRepository;
import com.waytoearth.repository.emblem.UserEmblemRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    // 엠블럼 요약 계산용 리포지토리 주입
    private final UserEmblemRepository userEmblemRepository;
    private final EmblemRepository emblemRepository;

    private final FileService fileService; // ✅ 주입 추가

    /**
     * 카카오 ID로 사용자 조회
     */
    public User findByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId).orElse(null);
    }

    /**
     * 사용자 ID로 조회
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 신규 사용자 생성
     */
    @Transactional
    public User createUser(Long kakaoId) {
        log.info("[UserService] 신규 사용자 생성 - kakaoId: {}", kakaoId);
        User newUser = User.builder()
                .kakaoId(kakaoId)
                .build();
        return userRepository.save(newUser);
    }

    /**
     * 온보딩 완료 처리
     */
    @Transactional
    public void completeOnboarding(Long userId, OnboardingRequest request) {
        log.info("[UserService] 온보딩 완료 요청 - userId: {}, nickname: {}", userId, request.getNickname());

        User user = findById(userId);

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        user.completeOnboarding(
                request.getNickname(),
                request.getResidence(),
                request.getAge_group(),
                request.getGender(),
                request.getWeekly_goal_distance(),
                request.getProfileImageUrl()
        );

        userRepository.save(user);
        log.info("[UserService] 온보딩 완료 - userId: {}", userId);
    }

    /**
     * 닉네임 중복 검사
     */
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // =========================
    // 👇 여기부터 추가된 구현
    // =========================

    /**
     * 내 정보 조회 (GET /v1/users/me)
     */

    public UserInfoResponse getMe(Long userId) {
        User u = findById(userId);

        String ageGroup = (u.getAgeGroup() == null) ? null : u.getAgeGroup().name();
        String gender   = (u.getGender() == null) ? null : u.getGender().name();
        Instant created = (u.getCreatedAt() == null) ? null : u.getCreatedAt()
                .atOffset(ZoneOffset.UTC).toInstant();

        // ✅ profileImageKey로 presigned GET URL 발급
        String profileImageUrl = null;
        if (u.getProfileImageKey() != null && !u.getProfileImageKey().isEmpty()) {
            profileImageUrl = fileService.createPresignedGetUrl(u.getProfileImageKey());
        }

        return new UserInfoResponse(
                u.getId(),
                u.getNickname(),
                profileImageUrl,   // presigned GET URL
                u.getResidence(),
                ageGroup,
                gender,
                u.getWeeklyGoalDistance(),
                u.getTotalDistance(),
                u.getTotalRunningCount(),
                created,
                u.getProfileImageKey()
        );
    }

    /**
     * 내 정보 요약 (GET /v1/users/me/summary)
     * - 보유 엠블럼 수 / 전체 엠블럼 수 = completion_rate
     * - 총거리/러닝 횟수는 users 테이블의 캐시 필드 사용
     */
    public UserSummaryResponse getSummary(Long userId) {
        int owned = (int) userEmblemRepository.countByUserId(userId);
        int total = (int) emblemRepository.count();
        double completion = (total == 0) ? 0.0 : (owned * 1.0 / total);

        User u = findById(userId);
        double totalDistance = (u.getTotalDistance() == null) ? 0.0 : u.getTotalDistance().doubleValue();
        int totalCount = (u.getTotalRunningCount() == null) ? 0 : u.getTotalRunningCount();

        return new UserSummaryResponse(completion, owned, totalDistance, totalCount);
    }

    /**
     * 프로필 수정 (PUT /v1/users/me)
     * - 닉네임, 프로필 이미지 URL, 거주지, 주간 목표 거리 (부분 수정 가능)
     * - 닉네임은 변경 시 중복 체크
     */
    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest req) {
        User u = findById(userId);

        // 닉네임
        if (req.getNickname() != null) {
            String newNickname = req.getNickname().trim();
            if (!newNickname.isEmpty() && !newNickname.equals(u.getNickname())) {
                if (userRepository.existsByNickname(newNickname)) {
                    throw new DuplicateResourceException("닉네임이 이미 존재합니다.");
                }
                u.setNickname(newNickname);
            }
        }

        // 프로필 이미지 (Key만 저장)
        if (req.getProfileImageKey() != null && !req.getProfileImageKey().trim().isEmpty()) {
            u.setProfileImageKey(req.getProfileImageKey().trim());
        }

        // 거주지
        if (req.getResidence() != null && !req.getResidence().trim().isEmpty()) {
            u.setResidence(req.getResidence().trim());
        }

        // 주간 목표 거리
        if (req.getWeeklyGoalDistance() != null) {
            var normalized = req.getWeeklyGoalDistance().setScale(2, RoundingMode.HALF_UP);
            u.setWeeklyGoalDistance(normalized);
        }

        userRepository.save(u);

        log.info("[Users:Update] userId={}, nickname={}, residence={}, weeklyGoalDistance={}, profileImageKey={}",
                u.getId(), u.getNickname(), u.getResidence(), u.getWeeklyGoalDistance(), u.getProfileImageKey());
    }



    @Transactional
    public void removeProfileImage(Long userId) {
        User u = findById(userId);
        if (u.getProfileImageKey() != null) {
            fileService.deleteObject(u.getProfileImageKey());
            u.setProfileImageKey(null);
            u.setProfileImageUrl(null); // 기본이미지 처리 가능
        }
        userRepository.save(u);
    }

}
