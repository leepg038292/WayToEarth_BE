package com.waytoearth.service.auth;

import com.waytoearth.dto.request.auth.KakaoLoginRequest;
import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.request.auth.TokenRefreshRequest;
import com.waytoearth.dto.response.auth.KakaoUserInfo;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.auth.OnboardingResponse;
import com.waytoearth.dto.response.auth.TokenRefreshResponse;
import com.waytoearth.entity.auth.RefreshToken;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.UnauthorizedException;
import com.waytoearth.repository.auth.RefreshTokenRepository;
import com.waytoearth.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final KakaoApiService kakaoApiService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

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

        // 4. 액세스 토큰 생성 (15분)
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());

        // 5. 리프레시 토큰 생성 (30일) 및 Redis 저장
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .issuedAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("[AuthService] 로그인 완료 - userId: {}, role: {}, isNewUser: {}, isOnboardingCompleted: {}",
                user.getId(), user.getRole(), isNewUser, user.getIsOnboardingCompleted());

        return LoginResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
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

    /**
     * 액세스 토큰 재발급 (조건부 리프레시 토큰 재발급)
     * - 리프레시 토큰 만료까지 7일 이하 남으면 리프레시 토큰도 재발급
     */
    @Transactional
    public TokenRefreshResponse refreshAccessToken(String refreshTokenValue) {
        log.info("[AuthService] 토큰 재발급 요청");

        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

        // 2. Redis에 저장된 토큰과 비교
        RefreshToken savedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("로그아웃된 사용자입니다"));

        if (!savedToken.getToken().equals(refreshTokenValue)) {
            // 탈취 의심 → 모든 리프레시 토큰 삭제
            refreshTokenRepository.deleteById(userId);
            log.warn("[SECURITY] 리프레시 토큰 불일치 감지 - userId: {}", userId);
            throw new UnauthorizedException("토큰 불일치 - 재로그인이 필요합니다");
        }

        // 3. 새 액세스 토큰 발급
        User user = userService.findById(userId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());

        // 4. 조건부 리프레시 토큰 재발급 (만료까지 7일 이하 남으면)
        Long remainingDays = jwtTokenProvider.getRemainingDays(refreshTokenValue);
        String newRefreshToken = null;

        if (remainingDays <= 7) {
            newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            savedToken.updateToken(newRefreshToken);
            refreshTokenRepository.save(savedToken);

            log.info("[AuthService] 리프레시 토큰 재발급 - userId: {}, 남은 기간: {}일", userId, remainingDays);
        }

        log.info("[AuthService] 액세스 토큰 재발급 완료 - userId: {}", userId);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)  // 재발급 안 했으면 null
                .build();
    }

    /**
     * 로그아웃
     * - Redis에서 리프레시 토큰 삭제
     * - 액세스 토큰 블랙리스트 추가
     */
    @Transactional
    public void logout(Long userId, String accessToken) {
        log.info("[AuthService] 로그아웃 시작 - userId: {}", userId);

        // 1. Redis에서 리프레시 토큰 삭제
        refreshTokenRepository.deleteById(userId);

        // 2. 액세스 토큰 블랙리스트 추가 (남은 시간만큼만 저장)
        Long expirationTime = jwtTokenProvider.getExpirationTime(accessToken);
        if (expirationTime > 0) {
            tokenBlacklistService.addToBlacklist(accessToken, expirationTime);
        }

        log.info("[AuthService] 로그아웃 완료 - userId: {}", userId);
    }
}