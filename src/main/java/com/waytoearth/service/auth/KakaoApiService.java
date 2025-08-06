package com.waytoearth.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waytoearth.dto.response.auth.KakaoUserInfo;
import com.waytoearth.exception.UnauthorizedException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoApiService {

    private final WebClient kakaoAuthWebClient;
    private final WebClient kakaoApiWebClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * Authorization Code로 카카오 Access Token 발급
     */
    public String getKakaoAccessToken(String authorizationCode) {
        log.info("[KakaoApiService] 카카오 토큰 요청 시작 - code: {}", authorizationCode);
        log.info("[DEBUG] redirect_uri used for Kakao token request = {}", redirectUri);  // ✅ 추가!


        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("redirect_uri", redirectUri);
            formData.add("code", authorizationCode);

            KakaoTokenDto kakaoTokenDto = kakaoAuthWebClient.post()
                    .uri("/oauth/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KakaoTokenDto.class)
                    .block();

            Objects.requireNonNull(kakaoTokenDto, "카카오 토큰 응답이 null입니다");

            log.info("[KakaoApiService] 카카오 토큰 발급 성공");
            return kakaoTokenDto.getAccessToken();

        } catch (WebClientResponseException e) {
            log.error("[KakaoApiService] 카카오 토큰 발급 실패 - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 인증에 실패했습니다: 유효하지 않은 인가 코드", e);
        } catch (Exception e) {
            log.error("[KakaoApiService] 카카오 토큰 발급 중 예상치 못한 에러", e);
            throw new RuntimeException("카카오 토큰 발급 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        log.info("[KakaoApiService] 카카오 사용자 정보 조회 시작");

        try {
            KakaoUserInfo kakaoUserInfo = kakaoApiWebClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();

            Objects.requireNonNull(kakaoUserInfo, "카카오 사용자 정보 응답이 null입니다");

            log.info("[KakaoApiService] 카카오 사용자 정보 조회 성공 - kakaoId: {}", kakaoUserInfo.getId());
            return kakaoUserInfo;

        } catch (WebClientResponseException e) {
            log.error("[KakaoApiService] 카카오 사용자 정보 조회 실패 - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 사용자 정보 조회에 실패했습니다", e);
        } catch (Exception e) {
            log.error("[KakaoApiService] 카카오 사용자 정보 조회 중 예상치 못한 에러", e);
            throw new RuntimeException("카카오 사용자 정보 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 카카오 API 응답용 내부 DTO
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class KakaoTokenDto {
        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("refresh_token_expires_in")
        private Integer refreshTokenExpiresIn;

        @JsonProperty("scope")
        private String scope;
    }
}