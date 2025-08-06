package com.waytoearth.service.user;

import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.entity.User;
import com.waytoearth.exception.DuplicateResourceException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 카카오 ID로 사용자 조회
     */
    public User findByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElse(null);
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

        // 1. 사용자 조회
        User user = findById(userId);

        // 2. 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        // 3. 온보딩 정보 업데이트
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
}