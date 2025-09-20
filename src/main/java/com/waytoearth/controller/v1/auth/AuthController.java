package com.waytoearth.controller.v1.auth;

import com.waytoearth.dto.request.auth.KakaoLoginRequest;
import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.auth.OnboardingResponse;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.auth.AuthService;
import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증 API", description = "카카오 로그인 및 사용자 인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService; // 닉네임 중복 확인용

    @Operation(summary = "카카오 로그인", description = "카카오 SDK에서 받은 액세스 토큰으로 로그인 처리")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 액세스 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @Parameter(description = "카카오 로그인 요청", required = true)
            @RequestBody @Valid KakaoLoginRequest request) {

        log.info("[AuthController] 카카오 로그인 요청 - accessToken: {}, kakaoId: {}", 
                request.getAccessToken(), request.getKakaoId());

        LoginResponse response = authService.loginWithKakaoAccessToken(request);

        log.info("[AuthController] 카카오 로그인 완료 - userId: {}, isNewUser: {}",
                response.getUserId(), response.getIsNewUser());

        return ResponseEntity.ok(ApiResponse.success(response, "카카오 로그인에 성공했습니다."));
    }

    @Operation(
            summary = "온보딩 완료",
            description = "신규 사용자 온보딩 정보 입력",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "온보딩 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardingResponse>> completeOnboarding(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "온보딩 정보", required = true)
            @RequestBody @Valid OnboardingRequest request) {

        log.info("[AuthController] 온보딩 완료 요청 - userId: {}, nickname: {}",
                user.getUserId(), request.getNickname());

        OnboardingResponse response = authService.completeOnboarding(user.getUserId(), request);

        log.info("[AuthController] 온보딩 완료 - userId: {}", response.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "온보딩이 완료되었습니다."));
    }

    // ===============================
    //  닉네임 중복 확인 (추가)
    // ===============================
    @Operation(
            summary = "닉네임 중복 확인",
            description = "닉네임 사용 가능 여부를 확인합니다. (무인증)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkNickname(
            @Parameter(description = "중복 확인할 닉네임", required = true)
            @RequestParam String nickname) {

        log.info("[AuthController] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean available = !userService.existsByNickname(nickname);

        Map<String, Boolean> data = Map.of("available", available);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";

        log.info("[AuthController] 닉네임 중복 확인 결과 - nickname: {}, available: {}", nickname, available);
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
}
