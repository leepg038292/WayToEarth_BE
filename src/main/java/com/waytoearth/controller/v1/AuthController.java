package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.auth.KakaoLoginRequest;
import com.waytoearth.dto.request.auth.OnboardingRequest;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.auth.OnboardingResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.auth.AuthService;
import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final UserService userService; // ✅ 닉네임 중복 확인용

    @Operation(summary = "카카오 로그인", description = "카카오 Authorization Code로 로그인 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/kakao")
    public ResponseEntity<LoginResponse> kakaoLogin(
            @Parameter(description = "카카오 로그인 요청", required = true)
            @RequestBody @Valid KakaoLoginRequest request) {

        log.info("[AuthController] 카카오 로그인 요청 - authorizationCode: {}", request.getCode());

        LoginResponse response = authService.loginWithKakaoCode(request.getCode());

        log.info("[AuthController] 카카오 로그인 완료 - userId: {}, isNewUser: {}",
                response.getUserId(), response.getIsNewUser());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "온보딩 완료",
            description = "신규 사용자 온보딩 정보 입력",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<OnboardingResponse> completeOnboarding(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "온보딩 정보", required = true)
            @RequestBody @Valid OnboardingRequest request) {

        log.info("[AuthController] 온보딩 완료 요청 - userId: {}, nickname: {}",
                user.getUserId(), request.getNickname());

        OnboardingResponse response = authService.completeOnboarding(user.getUserId(), request);

        log.info("[AuthController] 온보딩 완료 - userId: {}", response.getUserId());
        return ResponseEntity.ok(response);
    }

    // ===============================
    // ✅ 닉네임 중복 확인 (추가)
    // ===============================
    @Operation(
            summary = "닉네임 중복 확인",
            description = "닉네임 사용 가능 여부를 확인합니다. (무인증)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(
            @Parameter(description = "중복 확인할 닉네임", required = true)
            @RequestParam String nickname) {

        log.info("[AuthController] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean available = !userService.existsByNickname(nickname);

        Map<String, Object> body = Map.of(
                "available", available,
                "message", available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
        );

        log.info("[AuthController] 닉네임 중복 확인 결과 - nickname: {}, available: {}", nickname, available);
        return ResponseEntity.ok(body);
    }
}
