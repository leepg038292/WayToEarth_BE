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
import com.waytoearth.repository.journey.UserJourneyProgressRepository;
import com.waytoearth.repository.running.RunningRecordRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.feed.FeedRepository;
import com.waytoearth.repository.feed.FeedLikeRepository;
import com.waytoearth.repository.journey.GuestbookRepository;
import com.waytoearth.repository.crew.CrewJoinRequestRepository;
import com.waytoearth.repository.notification.FcmTokenRepository;
import com.waytoearth.repository.notification.NotificationSettingRepository;
import com.waytoearth.repository.crew.CrewChatReadStatusRepository;
import com.waytoearth.repository.crew.CrewChatNotificationSettingRepository;
import com.waytoearth.repository.crew.CrewChatRepository;
import com.waytoearth.service.auth.KakaoApiService;
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

    private final FileService fileService; //  주입 추가

    // 회원 탈퇴를 위한 리포지토리들
    private final UserJourneyProgressRepository userJourneyProgressRepository;
    private final RunningRecordRepository runningRecordRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final GuestbookRepository guestbookRepository;
    private final CrewJoinRequestRepository crewJoinRequestRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final CrewChatReadStatusRepository crewChatReadStatusRepository;
    private final CrewChatNotificationSettingRepository crewChatNotificationSettingRepository;
    private final CrewChatRepository crewChatRepository;

    // 카카오 연동 해제를 위한 서비스
    private final KakaoApiService kakaoApiService;

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
                request.getProfileImageKey()
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

        //  profileImageKey로 presigned GET URL 발급
        String profileImageUrl = null;
        if (u.getProfileImageKey() != null && !u.getProfileImageKey().isEmpty()) {
            profileImageUrl = fileService.createPresignedGetUrl(u.getProfileImageKey());
        }

        String role = (u.getRole() == null) ? null : u.getRole().name();

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
                u.getProfileImageKey(),
                role
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

    /**
     * 회원 탈퇴 (사용자 및 연관 데이터 완전 삭제)
     * - 카카오 연동을 해제합니다 (카카오 계정은 유지됨)
     * - 연관된 모든 데이터를 안전하게 삭제합니다
     * - 프로필 이미지가 있다면 S3에서도 삭제합니다
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("[UserService] 회원 탈퇴 시작 - userId: {}", userId);

        // 1. 사용자 존재 확인
        User user = findById(userId);

        // 2. 카카오 연동 해제
        try {
            kakaoApiService.unlinkKakaoAccount(user.getKakaoId());
            log.info("[UserService] 카카오 연동 해제 완료 - kakaoId: {}", user.getKakaoId());
        } catch (Exception e) {
            log.warn("[UserService] 카카오 연동 해제 실패 (계속 진행) - kakaoId: {}, error: {}",
                     user.getKakaoId(), e.getMessage());
        }

        // 3. 프로필 이미지가 있다면 S3에서 삭제
        if (user.getProfileImageKey() != null && !user.getProfileImageKey().isEmpty()) {
            try {
                fileService.deleteObject(user.getProfileImageKey());
                log.info("[UserService] 프로필 이미지 삭제 완료 - key: {}", user.getProfileImageKey());
            } catch (Exception e) {
                log.warn("[UserService] 프로필 이미지 삭제 실패 (계속 진행) - key: {}, error: {}",
                         user.getProfileImageKey(), e.getMessage());
            }
        }

        // 4. 연관 데이터 삭제 (외래키 제약 순서 고려)
        log.info("[UserService] 연관 데이터 삭제 시작 - userId: {}", userId);

        // 4-1. 피드 좋아요 삭제 (FeedLike -> Feed 참조)
        feedLikeRepository.deleteByUserId(userId);
        log.debug("[UserService] 피드 좋아요 삭제 완료");

        // 4-2. 피드 삭제 (Feed -> RunningRecord 참조)
        feedRepository.deleteByUserId(userId);
        log.debug("[UserService] 피드 삭제 완료");

        // 4-3. 방명록 삭제
        guestbookRepository.deleteByUserId(userId);
        log.debug("[UserService] 방명록 삭제 완료");

        // 4-4. 크루 가입 신청 삭제
        crewJoinRequestRepository.deleteByUserId(userId);
        log.debug("[UserService] 크루 가입 신청 삭제 완료");

        // 4-5. 크루 멤버십 삭제
        crewMemberRepository.deleteByUserId(userId);
        log.debug("[UserService] 크루 멤버십 삭제 완료");

        // 4-6. 러닝 기록 삭제 (RunningRoute는 cascade로 자동 삭제됨)
        runningRecordRepository.deleteByUserId(userId);
        log.debug("[UserService] 러닝 기록 삭제 완료");

        // 4-7. 여행 진행 내역 삭제 (StampEntity는 cascade로 자동 삭제됨)
        userJourneyProgressRepository.deleteByUserId(userId);
        log.debug("[UserService] 여행 진행 내역 삭제 완료");

        // 4-8. 사용자 엠블럼 삭제
        userEmblemRepository.deleteByUserId(userId);
        log.debug("[UserService] 사용자 엠블럼 삭제 완료");

        // 4-9. FCM 토큰 삭제
        fcmTokenRepository.deleteByUserId(userId);
        log.debug("[UserService] FCM 토큰 삭제 완료");

        // 4-10. 알림 설정 삭제 (글로벌)
        notificationSettingRepository.deleteByUserId(userId);
        log.debug("[UserService] 알림 설정 삭제 완료");

        // 4-11. 크루 채팅 읽음 상태 삭제
        crewChatReadStatusRepository.deleteByReaderId(userId);
        log.debug("[UserService] 크루 채팅 읽음 상태 삭제 완료");

        // 4-12. 크루 채팅 알림 설정 삭제
        crewChatNotificationSettingRepository.deleteByUserId(userId);
        log.debug("[UserService] 크루 채팅 알림 설정 삭제 완료");

        // 4-13. 사용자가 보낸 채팅은 보존: 발신자를 '탈퇴한 사용자'로 치환
        User deletedSentinel = getOrCreateDeletedUserSentinel();
        int reassigned = crewChatRepository.reassignSenderToDeleted(userId, deletedSentinel);
        log.debug("[UserService] 보낸 채팅 발신자 치환 완료 - reassigned: {}", reassigned);

        // 5. 최종적으로 사용자 삭제
        userRepository.delete(user);
        log.info("[UserService] 회원 탈퇴 완료 - userId: {}, kakaoId: {}", userId, user.getKakaoId());
    }

    private User getOrCreateDeletedUserSentinel() {
        // kakaoId -1을 가진 예약 사용자 사용
        Long sentinelKakaoId = -1L;
        return userRepository.findByKakaoId(sentinelKakaoId).orElseGet(() -> {
            User sentinel = User.builder()
                    .kakaoId(sentinelKakaoId)
                    .nickname("탈퇴한 사용자")
                    .isOnboardingCompleted(true)
                    .build();
            return userRepository.save(sentinel);
        });
    }

}
