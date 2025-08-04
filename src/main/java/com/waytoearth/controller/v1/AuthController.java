package com.waytoearth.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.waytoearth.dto.request.auth.KakaoLoginRequest;
import com.waytoearth.dto.response.auth.KakaoTokenResponse;
import com.waytoearth.service.auth.KakaoApiService;

@Tag(name = "인증 API", description = "카카오 로그인 및 사용자 인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KakaoApiService kakaoApiService;

    @Operation(summary = "카카오 로그인 콜백", description = "카카오에서 리다이렉트된 인가 코드 처리 (브라우저 리다이렉트용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/kakao")
    public ResponseEntity<KakaoTokenResponse> kakaoCallback(
            @Parameter(description = "카카오 인가 코드", required = true, example = "abc123xyz")
            @RequestParam("code") String code) {

        log.info("[AuthController] 카카오 콜백 받음 - code: {}", code);

        // 카카오에서 토큰 발급받기
        KakaoTokenResponse response = kakaoApiService.getKakaoTokens(code);

        log.info("[AuthController] 카카오 토큰 발급 완료 (GET)");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오 로그인", description = "프론트에서 인가 코드를 직접 전송하여 토큰 발급 (API 호출용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/kakao")
    public ResponseEntity<KakaoTokenResponse> kakaoLogin(
            @Parameter(description = "카카오 로그인 요청", required = true)
            @RequestBody @Valid KakaoLoginRequest request) {

        log.info("[AuthController] 카카오 로그인 요청 - authorizationCode: {}", request.getAuthorizationCode());

        // 카카오에서 토큰 발급받기
        KakaoTokenResponse response = kakaoApiService.getKakaoTokens(request.getAuthorizationCode());

        log.info("[AuthController] 카카오 토큰 발급 완료 (POST)");
        return ResponseEntity.ok(response);
    }
}