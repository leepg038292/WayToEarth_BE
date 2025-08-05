package com.waytoearth.service.auth;

import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.response.auth.KakaoUserInfo;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.auth.OnboardingResponse;
import com.waytoearth.entity.User;
import com.waytoearth.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final KakaoApiService kakaoApiService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 카카오 Authorization Code로 로그인 처리
     */
    @Transactional
    public LoginResponse loginWithKakaoCode(String authorizationCode) {
        log.info("[AuthService] 카카오 로그인 시작 - authorizationCode: {}", authorizationCode);

        // 1. 카카오에서 Access Token 발급
        String kakaoAccessToken = kakaoApiService.getKakaoAccessToken(authorizationCode);
        log.info("[AuthService] 카카오 액세스 토큰 발급 완료");

        // 2. 카카오에서 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoApiService.getKakaoUserInfo(kakaoAccessToken);
        log.info("[AuthService] 카카오 사용자 정보 조회 완료 - kakaoId: {}", kakaoUserInfo.getId());

        // 3. 기존 사용자 조회 또는 신규 생성
        User existingUser = userService.findByKakaoId(kakaoUserInfo.getId());
        boolean isNewUser = (existingUser == null);

        User user = isNewUser ?
                userService.createUser(kakaoUserInfo.getId()) :
                existingUser;

        // 4. JWT 토큰 생성
        String jwtToken = jwtTokenProvider.generateToken(user.getId());

        log.info("[AuthService] 로그인 완료 - userId: {}, isNewUser: {}, isOnboardingCompleted: {}",
                user.getId(), isNewUser, user.getIsOnboardingCompleted());

        return LoginResponse.builder()
                .userId(user.getId())
                .jwtToken(jwtToken)
                .isNewUser(isNewUser)
                .isOnboardingCompleted(user.getIsOnboardingCompleted())
                .build();
    }

    /**
     * 온보딩 완료 처리 (userId 기반)
     */
    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        log.info("[AuthService] 온보딩 완료 요청 - userId: {}, nickname: {}", userId, request.getNickname());

        // UserService에 온보딩 처리 위임
        userService.completeOnboarding(userId, request);

        log.info("[AuthService] 온보딩 완료 - userId: {}", userId);

        return OnboardingResponse.builder()
                .userId(userId)
                .message("온보딩이 완료되었습니다.")
                .build();
    }
}