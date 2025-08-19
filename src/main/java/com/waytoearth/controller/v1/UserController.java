package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.user.UserUpdateRequest;
import com.waytoearth.dto.response.user.UserInfoResponse;
import com.waytoearth.dto.response.user.UserSummaryResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 정보 조회 및 관리 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "사용자가 입력한 닉네임이 이미 존재하는지 확인합니다. 인증이 필요하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 중복 여부 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "중복인 경우",
                                            value = "{\"isDuplicate\": true, \"message\": \"이미 사용 중인 닉네임입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "중복이 아닌 경우",
                                            value = "{\"isDuplicate\": false, \"message\": \"사용 가능한 닉네임입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknameDuplicate(
            @Parameter(description = "중복 확인할 닉네임", example = "runner_kim", required = true)
            @RequestParam String nickname
    ) {
        log.info("[UserController] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean isDuplicate = userService.existsByNickname(nickname);
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        response.put("message", isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");

        log.info("[UserController] 닉네임 중복 확인 결과 - nickname: {}, isDuplicate: {}", nickname, isDuplicate);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user
    ) {
        log.info("[UserController] 내 정보 조회 요청 - userId: {}", user.getUserId());

        UserInfoResponse response = userService.getMe(user.getUserId());

        log.info("[UserController] 내 정보 조회 완료 - userId: {}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 정보 요약 조회",
            description = "총 거리, 러닝 횟수, 엠블럼 완성도 등 사용자의 요약 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 요약 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/me/summary")
    public ResponseEntity<UserSummaryResponse> getMySummary(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user
    ) {
        log.info("[UserController] 내 정보 요약 조회 요청 - userId: {}", user.getUserId());

        UserSummaryResponse response = userService.getSummary(user.getUserId());

        log.info("[UserController] 내 정보 요약 조회 완료 - userId: {}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로필 수정",
            description = "사용자의 프로필 정보를 수정합니다. 닉네임, 프로필 이미지, 거주지, 주간 목표 거리를 부분적으로 수정할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = "{\"message\": \"프로필이 수정되었습니다.\", \"updated_at\": \"2024-01-15T10:30:00Z\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 닉네임 중복"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "수정할 프로필 정보", required = true)
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("[UserController] 프로필 수정 요청 - userId: {}, request: {}", user.getUserId(), request);

        userService.updateProfile(user.getUserId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "프로필이 수정되었습니다.");
        response.put("updated_at", Instant.now().toString());

        log.info("[UserController] 프로필 수정 완료 - userId: {}", user.getUserId());
        return ResponseEntity.ok(response);
    }
}