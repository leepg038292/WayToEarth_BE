package com.waytoearth.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.waytoearth.dto.response.auth.KakaoTokenResponse;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoApiService {

    private final WebClient kakaoAuthWebClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponse getKakaoTokens(String authorizationCode) {
        log.info("[KakaoApiService] 카카오 토큰 요청 시작 - code: {}", authorizationCode);
        log.info("[KakaoApiService] client-id: {}", clientId);
        log.info("[KakaoApiService] redirect-uri: {}", redirectUri);

        try {
            KakaoTokenDto kakaoTokenDto = kakaoAuthWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/oauth/token")
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("client_id", clientId)
                            .queryParam("redirect_uri", redirectUri)
                            .queryParam("code", authorizationCode)
                            .build(true))
                    .retrieve()
                    .bodyToMono(KakaoTokenDto.class)
                    .block();

            Objects.requireNonNull(kakaoTokenDto, "카카오 토큰 응답이 null입니다");

            log.info("[KakaoApiService] 카카오 토큰 발급 성공");
            log.info("[KakaoApiService] access_token: {}", kakaoTokenDto.getAccessToken());
            log.info("[KakaoApiService] expires_in: {}", kakaoTokenDto.getExpiresIn());
            log.info("[KakaoApiService] client-id: {}", clientId);
            log.info("[KakaoApiService] redirect-uri: {}", redirectUri);

            return KakaoTokenResponse.builder()
                    .accessToken(kakaoTokenDto.getAccessToken())
                    .refreshToken(kakaoTokenDto.getRefreshToken())
                    .tokenType(kakaoTokenDto.getTokenType())
                    .expiresIn(kakaoTokenDto.getExpiresIn())
                    .build();

        } catch (WebClientResponseException e) {
            log.error("[KakaoApiService] 카카오 API 에러 - Status: {}", e.getStatusCode());
            log.error("[KakaoApiService] 카카오 API 에러 - Response Body: {}", e.getResponseBodyAsString());
            log.error("[KakaoApiService] 카카오 API 에러 - Headers: {}", e.getHeaders());
            throw e;
        } catch (Exception e) {
            log.error("[KakaoApiService] 예상치 못한 에러", e);
            throw e;
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