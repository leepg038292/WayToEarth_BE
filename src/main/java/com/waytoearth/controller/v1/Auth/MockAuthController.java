package com.waytoearth.controller.v1.Auth;

import com.waytoearth.dto.request.auth.MockLoginRequest;
import com.waytoearth.dto.response.auth.LoginResponse;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.service.auth.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mock 인증 API", description = "테스트용 Mock 로그인 API (postman 프로파일에서만 사용)")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Profile("postman")
public class MockAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    
    @Operation(
        summary = "Mock 로그인", 
        description = "테스트용 Mock 로그인으로 JWT 토큰을 발급받습니다. postman 프로파일에서만 사용 가능합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mock 로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/mock-login")
    public ResponseEntity<ApiResponse<LoginResponse>> mockLogin(
            @Parameter(description = "Mock 로그인 요청", required = true)
            @RequestBody @Valid MockLoginRequest request) {
        
        log.info("[MockAuthController] Mock 로그인 요청 - userId: {}", request.getUserId());
        
        // JWT 토큰 발급
        String jwtToken = jwtTokenProvider.generateToken(request.getUserId());
        
        // Mock Login Response 생성
        LoginResponse response = LoginResponse.builder()
                .userId(request.getUserId())
                .jwtToken(jwtToken)
                .isNewUser(false) // Mock에서는 기존 사용자로 처리
                .isOnboardingCompleted(true) // Mock에서는 온보딩 완료로 처리
                .build();
        
        log.info("[MockAuthController] Mock 로그인 완료 - userId: {}, tokenGenerated: true", request.getUserId());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Mock 로그인에 성공했습니다."));
    }
}