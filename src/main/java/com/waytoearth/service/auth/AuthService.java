package com.waytoearth.service.auth;

import com.waytoearth.dto.request.auth.KakaoLoginRequest;
import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.response.auth.KakaoUserInfo;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.auth.OnboardingResponse;
import com.waytoearth.entity.user.User;
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
     * 카카오 SDK 액세스 토큰으로 로그인 처리
     */
    @Transactional
    public LoginResponse loginWithKakaoAccessToken(KakaoLoginRequest request) {
        log.info("[AuthService] 카카오 로그인 시작 - kakaoId: {}", request.getKakaoId());

        // 1. 카카오 액세스 토큰으로 사용자 정보 조회 및 검증
        KakaoUserInfo kakaoUserInfo = kakaoApiService.verifyAndGetKakaoUserInfo(request.getAccessToken());
        log.info("[AuthService] 카카오 사용자 정보 조회 완료 - kakaoId: {}", kakaoUserInfo.getId());

        // 2. 토큰에서 가져온 사용자 ID와 요청의 사용자 ID 일치 여부 확인 (보안)
        String tokenUserId = String.valueOf(kakaoUserInfo.getId());
        String requestUserId = String.valueOf(request.getKakaoId());

        if (!tokenUserId.equals(requestUserId)) {
            log.warn("[AuthService] 카카오 사용자 ID 불일치 - token: {}, request: {}",
                    tokenUserId, requestUserId);
            throw new IllegalArgumentException("토큰과 사용자 ID가 일치하지 않습니다");
        }


        // 3. 기존 사용자 조회 또는 신규 생성
        User existingUser = userService.findByKakaoId(kakaoUserInfo.getId());
        boolean isNewUser = (existingUser == null);

        User user = isNewUser ?
                userService.createUser(kakaoUserInfo.getId()) :
                existingUser;

        // 4. JWT 토큰 생성 (사용자의 실제 role 포함)
        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        log.info("[AuthService] 로그인 완료 - userId: {}, role: {}, isNewUser: {}, isOnboardingCompleted: {}",
                user.getId(), user.getRole(), isNewUser, user.getIsOnboardingCompleted());

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